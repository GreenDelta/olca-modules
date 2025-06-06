package org.openlca.core.results.providers;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.util.Pair;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class LazyLibrarySolver implements ResultProvider {

	private final IDatabase db;
	private final LibReaderRegistry libs;
	private final HashSet<String> usedLibs = new HashSet<>();
	private final MatrixSolver solver;

	private final Demand demand;
	private final MatrixData foregroundData;
	private final ResultProvider foregroundSolution;
	private final MatrixData fullData;

	private double[] scalingVector;
	private double[] totalRequirements;
	private final TIntObjectHashMap<double[]> solutions = newCache();
	private final TIntObjectHashMap<double[]> techColumns = newCache();

	private double[] totalFlows;
	private final TIntObjectHashMap<double[]> flowColumns = newCache();
	private final TIntObjectHashMap<double[]> directFlows = newCache();
	private final TIntObjectHashMap<double[]> totalFlowsOfOne = newCache();

	private double[] totalImpacts;
	private Matrix flowImpacts;
	private final TIntObjectHashMap<double[]> directImpacts = newCache();
	private final TIntObjectHashMap<double[]> totalImpactsOfOne = newCache();

	private Double totalCosts;
	private double[] _costs;
	private final TIntObjectMap<Double> totalCostsOfOne;

	private LazyLibrarySolver(SolverContext context) {
		this.db = context.db();
		this.libs = context.libraries();
		this.solver = context.solver();
		this.demand = context.demand();
		this.foregroundData = context.data();
		this.fullData = new MatrixData();
		this.fullData.demand = demand;
		this.fullData.impactIndex = foregroundData.impactIndex;
		this.foregroundSolution = InversionResult.of(context)
				.calculate()
				.provider();
		this.totalCostsOfOne = foregroundData.costVector != null
				? new TIntObjectHashMap<>()
				: null;
	}

	private static TIntObjectHashMap<double[]> newCache() {
		return new TIntObjectHashMap<>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
	}

	private double[] put(int key, TIntObjectHashMap<double[]> cache, double[] v) {
		cache.put(key, v);
		return v;
	}

	public static ResultProvider solve(SolverContext context) {

		var provider = new LazyLibrarySolver(context);
		provider.initIndices();

		// calculate the scaling vector
		var s = provider.solutionOfOne(0);
		var scalingVector = Arrays.copyOf(s, s.length);
		var demand = provider.demand.value();
		for (int i = 0; i < scalingVector.length; i++) {
			scalingVector[i] *= demand;
		}
		provider.scalingVector = scalingVector;

		return provider;
	}

	private void initIndices() {

		// create the combined tech-flow index
		var fIdx = foregroundData.techIndex;
		var techIndex = new TechIndex();
		techIndex.addAll(fIdx);
		for (var r : libs.readers()) {
			usedLibs.add(r.libraryName());
			var libIndex = r.techIndex();
			if (libIndex != null) {
				techIndex.addAll(libIndex);
			}
		}

		// create the combined envi-flow index
		EnviIndex enviIndex = null;
		if (foregroundData.enviIndex != null) {
			enviIndex = foregroundData.enviIndex.isRegionalized()
					? EnviIndex.createRegionalized()
					: EnviIndex.create();
			enviIndex.addAll(foregroundData.enviIndex);
		}
		for (var libId : usedLibs) {
			var lib = libs.get(libId);
			var libIdx = lib.enviIndex();
			if (libIdx == null)
				continue;
			if (enviIndex == null) {
				enviIndex = libIdx.isRegionalized()
						? EnviIndex.createRegionalized()
						: EnviIndex.create();
			}
			enviIndex.addAll(libIdx);
		}

		fullData.techIndex = techIndex;
		fullData.enviIndex = enviIndex;
	}

	@Override
	public Demand demand() {
		return demand;
	}

	@Override
	public TechIndex techIndex() {
		return fullData.techIndex;
	}

	@Override
	public EnviIndex enviIndex() {
		return fullData.enviIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return fullData.impactIndex;
	}

	@Override
	public boolean hasCosts() {
		return foregroundData.costVector != null;
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double scalingFactorOf(int techFlow) {
		return scalingVector[techFlow];
	}

	@Override
	public double[] totalRequirements() {
		if (totalRequirements != null)
			return totalRequirements;

		// for a library product j, the total requirements
		// need to be based on the a_{jj} entry of the
		// technology matrix of the library because our
		// scaling factor for j is based on the inverse
		// of that matrix

		// handle the foreground system
		var index = fullData.techIndex;
		var t = new double[index.size()];
		var techF = foregroundData.techMatrix;
		for (int i = 0; i < techF.columns(); i++) {
			var product = index.at(i);
			if (product.isFromLibrary() && product.isProcess())
				continue;
			t[i] = techF.get(i, i) * scalingVector[i];
		}

		// handle the libraries
		for (var libId : usedLibs) {
			var lib = libs.get(libId);
			var indexB = lib.techIndex();
			if (indexB == null)
				continue;
			var libDiag = lib.diagonalOf(LibMatrix.A);
			if (libDiag == null)
				continue;
			for (int iB = 0; iB < libDiag.length; iB++) {
				var product = indexB.at(iB);
				var productLib = product.library();
				if (!Objects.equals(productLib, libId))
					continue;

				var i = index.of(product);
				if (i < 0)
					continue;
				var si = scalingVector[i];
				if (si == 0)
					continue;
				t[i] = si * libDiag[iB];
			}
		}

		totalRequirements = t;
		return totalRequirements;
	}

	@Override
	public double totalRequirementsOf(int techFlow) {
		var t = totalRequirements();
		return t[techFlow];
	}

	@Override
	public double[] techColumnOf(int techFlow) {
		var column = techColumns.get(techFlow);
		if (column != null)
			return column;

		var index = fullData.techIndex;
		var product = index.at(techFlow);
		column = new double[index.size()];
		var libId = product.library();

		// in case of a foreground product or result, we just need
		// to copy the column of the foreground system into the
		// first part of the result column as the tech. index of
		// the foreground system is exactly the first part of the
		// combined index
		if (libId == null || !product.isProcess()) {
			var colF = foregroundData.techMatrix.getColumn(techFlow);
			System.arraycopy(colF, 0, column, 0, colF.length);
			return put(techFlow, techColumns, column);
		}

		// in case of a library product, we need to map
		// the column entries
		var lib = libs.get(libId);
		var libIndex = lib.techIndex();
		if (libIndex == null)
			return column;
		int jLib = libIndex.of(product);
		var columnLib = lib.columnOf(LibMatrix.A, jLib);
		if (columnLib == null)
			return column;
		for (int iLib = 0; iLib < columnLib.length; iLib++) {
			double val = columnLib[iLib];
			if (val == 0)
				continue;
			var providerLib = libIndex.at(iLib);
			var i = index.of(providerLib);
			if (i < 0)
				continue;
			column[i] = val;
		}
		return put(techFlow, techColumns, column);
	}

	@Override
	public double[] solutionOfOne(int techFlow) {
		var solution = solutions.get(techFlow);
		if (solution != null)
			return solution;

		var techIndex = fullData.techIndex;
		solution = new double[techIndex.size()];

		// initialize a queue that is used for adding scaled
		// sub-solutions of libraries recursively
		var queue = new ArrayDeque<Pair<TechFlow, Double>>();
		var start = fullData.techIndex.at(techFlow);
		if (start.isFromLibrary() && start.isProcess()) {
			// start process is a library process
			queue.push(Pair.of(start, start.isWaste() ? -1.0 : 1.0));
		} else {
			// start process is a foreground process
			// we copy the values of the solution of
			// the foreground system or initialize
			// the entries of the queue with the scaled
			// library links
			var idxF = foregroundData.techIndex;
			var pf = idxF.of(start);
			var sf = foregroundSolution.solutionOfOne(pf);
			for (int i = 0; i < sf.length; i++) {
				var value = sf[i];
				if (value == 0)
					continue;
				var provider = idxF.at(i);
				if (provider.isFromLibrary() && provider.isProcess()) {
					queue.push(Pair.of(provider, value));
				} else {
					int index = techIndex.of(provider);
					solution[index] = value;
				}
			}
		}

		// recursively add library solutions
		while (!queue.isEmpty()) {
			var pair = queue.pop();
			var p = pair.first;
			var libId = p.library();
			if (libId == null)
				continue;
			double factor = p.isWaste()
					? -pair.second
					: pair.second;

			var lib = libs.get(libId);
			var libIndex = lib.techIndex();
			if (libIndex == null)
				continue;
			int column = libIndex.of(p);
			var libSolution = lib.columnOf(LibMatrix.INV, column);
			if (libSolution == null)
				continue;
			for (int i = 0; i < libSolution.length; i++) {
				var value = libSolution[i];
				if (value == 0)
					continue;
				var provider = libIndex.at(i);
				var subLibID = provider.library();
				if (Objects.equals(libId, subLibID)) {
					int index = techIndex.of(provider);
					solution[index] += factor * value;
				} else {
					queue.push(Pair.of(provider, factor * value));
				}
			}
		}

		return put(techFlow, solutions, solution);
	}

	@Override
	public double loopFactorOf(int techFlow) {
		var aii = techValueOf(techFlow, techFlow);
		var ii = solutionOfOne(techFlow)[techFlow];
		var f = aii * ii;
		return f == 0
				? 0
				: 1 / f;
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		var column = flowColumns.get(techFlow);
		if (column != null)
			return column;

		var flowIdx = fullData.enviIndex;
		if (flowIdx == null)
			return EMPTY_VECTOR;

		column = new double[flowIdx.size()];
		var product = fullData.techIndex.at(techFlow);
		var libId = product.library();

		// in case of a foreground product or result, we just need
		// to copy the column of the foreground system into the
		// first part of the result column, as the flow index of
		// the foreground system is exactly the first part of the
		// combined index
		if (libId == null || !product.isProcess()) {
			var flowMatrixF = foregroundData.enviMatrix;
			if (flowMatrixF != null) {
				var colF = flowMatrixF.getColumn(techFlow);
				System.arraycopy(colF, 0, column, 0, colF.length);
			}
			return put(techFlow, flowColumns, column);
		}

		// in case of a library product, we need to map
		// the column entries
		var lib = libs.get(libId);
		var flowIdxB = lib.enviIndex();
		var techIdxB = lib.techIndex();
		if (flowIdxB == null || techIdxB == null)
			return put(techFlow, flowColumns, column);
		var jB = techIdxB.of(product);
		var colB = lib.columnOf(LibMatrix.B, jB);
		if (colB == null)
			return put(techFlow, flowColumns, column);

		for (int iB = 0; iB < colB.length; iB++) {
			double val = colB[iB];
			if (val == 0)
				continue;
			var flow = flowIdxB.at(iB);
			var i = flowIdx.of(flow);
			if (i < 0)
				continue;
			column[i] = val;
		}

		return put(techFlow, flowColumns, column);
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		var flows = unscaledFlowsOf(product);
		return isEmpty(flows)
				? 0
				: flows[flow];
	}

	@Override
	public double[] directFlowsOf(int techFlow) {
		var flows = directFlows.get(techFlow);
		if (flows != null)
			return flows;
		var unscaled = unscaledFlowsOf(techFlow);
		if (isEmpty(unscaled))
			return EMPTY_VECTOR;
		var factor = scalingFactorOf(techFlow);
		flows = scale(unscaled, factor);
		return put(techFlow, directFlows, flows);
	}

	@Override
	public double directFlowOf(int enviFlow, int techFlow) {
		var flows = directFlowsOf(techFlow);
		return isEmpty(flows)
				? 0
				: flows[enviFlow];
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		var totals = totalFlowsOfOne.get(techFlow);
		if (totals != null)
			return totals;

		var flowIndex = fullData.enviIndex;
		if (flowIndex == null || flowIndex.isEmpty()) {
			return EMPTY_VECTOR;
		}

		var s = solutionOfOne(techFlow);
		totals = new double[flowIndex.size()];

		// add the foreground result
		var enviF = foregroundData.enviMatrix;
		if (enviF != null) {
			var sF = Arrays.copyOf(
					s, foregroundData.techIndex.size());
			var mF = solver.multiply(enviF, sF);
			System.arraycopy(mF, 0, totals, 0, mF.length);
		}

		var techIdx = techIndex();
		for (var libId : usedLibs) {
			var lib = libs.get(libId);
			var flowIdxB = lib.enviIndex();
			var techIdxB = lib.techIndex();
			if (flowIdxB == null || techIdxB == null)
				continue;
			if (flowIdxB.isEmpty())
				continue;

			// calculate the scaled library result
			var matrixB = lib.matrixOf(LibMatrix.B);
			if (matrixB == null)
				continue;
			var sB = new double[techIdxB.size()];
			for (int i = 0; i < s.length; i++) {
				var product = techIdx.at(i);
				var iB = techIdxB.of(product);
				if (iB < 0)
					continue;
				sB[iB] = s[i];
			}
			var gB = solver.multiply(matrixB, sB);
			for (int iB = 0; iB < gB.length; iB++) {
				var flow = flowIdxB.at(iB);
				var i = flowIndex.of(flow);
				if (i < 0)
					continue;
				totals[i] += gB[iB];
			}
		}
		return put(techFlow, totalFlowsOfOne, totals);
	}

	@Override
	public double[] totalFlows() {
		if (totalFlows != null)
			return totalFlows;
		var m = totalFlowsOfOne(0);
		var demandVal = demand.value();
		var results = Arrays.copyOf(m, m.length);
		for (int i = 0; i < m.length; i++) {
			results[i] *= demandVal;
		}
		totalFlows = results;
		return totalFlows;
	}

	/**
	 * Returns the characterization factors of the combined system.
	 * we cache this matrix in the `fullData` object.
	 */
	private MatrixReader impactFactors() {
		if (fullData.impactMatrix != null)
			return fullData.impactMatrix;
		if (!hasFlows() || !hasImpacts())
			return null;

		// allocate a Combined impact matrix C
		var impactIndex = impactIndex();
		var flowIndex = enviIndex();
		var builder = new MatrixBuilder();
		builder.minSize(impactIndex.size(), flowIndex.size());

		// collect factors for indicators from
		// the foreground database
		var impactsF = new ImpactIndex();
		impactIndex.each((index, impact) -> {
			if (!impact.isFromLibrary()) {
				impactsF.add(impact);
			}
		});
		if (!impactsF.isEmpty()) {
			var contexts = new HashSet<Long>();
			impactsF.each((_idx, impact) -> contexts.add(impact.id));
			// TODO: think about parameter redefinitions here:
			// a flow that is only used in a library could have
			// a formula for a characterization factor for an
			// impact category in the foreground database with
			// an parameter that is redefined in a calculation
			// setup -> unlikely? yes, but...
			var interpreter = ParameterTable.interpreter(
					db, contexts, Collections.emptyList());
			var matrixF = ImpactBuilder.of(db, flowIndex)
					.withImpacts(impactIndex)
					.withInterpreter(interpreter)
					.build().impactMatrix;
			if (matrixF != null) {
				// note that the combined flow index is used here
				// so that we do not have to map the columns
				matrixF.iterate((rowF, col, val) -> {
					var impact = impactsF.at(rowF);
					int row = impactIndex.of(impact);
					builder.set(row, col, val);
				});
			}
		}

		var usedLibs = new HashSet<String>();
		impactIndex.each((_idx, impact) -> {
			if (impact.library != null) {
				usedLibs.add(impact.library);
			}
		});
		for (var libId : usedLibs) {
			var lib = libs.get(libId);
			var libFlowIdx = lib.enviIndex();
			if (libFlowIdx == null)
				continue;
			var libImpactIdx = lib.impactIndex();
			if (libImpactIdx == null)
				continue;
			var libMatrix = lib.matrixOf(LibMatrix.C);
			if (libMatrix == null)
				continue;
			libMatrix.iterate((rowB, colB, val) -> {
				var impact = libImpactIdx.at(rowB);
				var flow = libFlowIdx.at(colB);
				int row = impactIndex.of(impact);
				int col = flowIndex.of(flow);
				if (row < 0 || col < 0)
					return;
				builder.set(row, col, val);
			});
		}

		ImpactBuilder.putVirtualFlowFactors(builder, impactIndex, flowIndex);

		fullData.impactMatrix = builder.finish();
		return fullData.impactMatrix;
	}

	@Override
	public double[] impactFactorsOf(int enviFlow) {
		var matrix = impactFactors();
		return matrix == null
				? EMPTY_VECTOR
				: matrix.getColumn(enviFlow);
	}

	@Override
	public double impactFactorOf(int indicator, int enviFlow) {
		var matrix = impactFactors();
		return matrix == null
				? 0
				: matrix.get(indicator, enviFlow);
	}

	private MatrixReader flowImpacts() {
		if (flowImpacts != null)
			return flowImpacts;
		var g = totalFlows();
		var factors = impactFactors();
		if (g == null || factors == null)
			return null;
		var m = factors.asMutableCopy();
		m.scaleColumns(g);
		flowImpacts = m;
		return flowImpacts;
	}

	@Override
	public double[] flowImpactsOf(int enviFlow) {
		var impacts = flowImpacts();
		return impacts == null
				? EMPTY_VECTOR
				: impacts.getColumn(enviFlow);
	}

	@Override
	public double flowImpactOf(int indicator, int enviFlow) {
		var impacts = flowImpacts();
		return impacts == null
				? 0
				: impacts.get(indicator, enviFlow);
	}

	@Override
	public double[] directImpactsOf(int techFlow) {
		var impacts = directImpacts.get(techFlow);
		if (impacts != null)
			return impacts;
		var factors = impactFactors();
		var flows = directFlowsOf(techFlow);
		if (factors == null || isEmpty(flows))
			return EMPTY_VECTOR;
		impacts = solver.multiply(factors, flows);
		return put(techFlow, directImpacts, impacts);
	}

	@Override
	public double[] totalImpactsOfOne(int techFlow) {
		var impacts = totalImpactsOfOne.get(techFlow);
		if (impacts != null)
			return impacts;
		var factors = impactFactors();
		var flows = totalFlowsOfOne(techFlow);
		if (factors == null || isEmpty(flows))
			return EMPTY_VECTOR;
		impacts = solver.multiply(factors, flows);
		return put(techFlow, totalImpactsOfOne, impacts);
	}

	@Override
	public double[] totalImpacts() {
		if (totalImpacts != null)
			return totalImpacts;
		var g = totalFlows();
		var factors = impactFactors();
		if (g == null || factors == null)
			return EMPTY_VECTOR;
		totalImpacts = solver.multiply(factors, g);
		return totalImpacts;
	}

	@Override
	public double directCostsOf(int techFlow) {
		return hasCosts()
				? scalingFactorOf(techFlow) * costs()[techFlow]
				: 0;
	}

	@Override
	public double totalCostsOfOne(int techFlow) {
		if (totalCostsOfOne == null)
			return 0;
		var cached = totalCostsOfOne.get(techFlow);
		if (cached != null)
			return cached;

		var s = solutionOfOne(techFlow);
		var c = solver.dot(s, costs());
		totalCostsOfOne.put(techFlow, c);
		return c;
	}

	@Override
	public double totalCosts() {
		if (!hasCosts())
			return 0;
		if (totalCosts != null)
			return totalCosts;
		totalCosts = solver.dot(scalingVector, costs());
		return totalCosts;
	}

	private double[] costs() {
		if (_costs != null)
			return _costs;

		var techIdx = techIndex();
		var costs = new double[techIdx.size()];
		var libCosts = LibCosts.allOf(usedLibs, libs);

		for (int i = 0; i < techIdx.size(); i++) {
			var techFlow = techIdx.at(i);

			if (techFlow.isFromLibrary()) {
				var cs = libCosts.get(techFlow.library());
				if (cs != null) {
					costs[i] = cs.get(techFlow);
				}
			} else {
				if (foregroundData.costVector == null)
					continue;
				var j = foregroundData.techIndex.of(techFlow);
				costs[i] = foregroundData.costVector[j];
			}
		}

		_costs = costs;
		return _costs;
	}

	private record LibCosts(TechIndex libIdx, double[] costs) {

		static Map<String, LibCosts> allOf(
				Set<String> usedLibs, LibReaderRegistry libs
		) {
			var map = new HashMap<String, LibCosts>();
			for (var libId : usedLibs) {
				var lib = libs.get(libId);
				if (lib == null || !lib.hasCostData())
					continue;
				map.put(libId, new LibCosts(lib.techIndex(), lib.costs()));
			}
			return map;
		}

		double get(TechFlow techFlow) {
			if (costs == null || libIdx == null)
				return 0;
			int i = libIdx.of(techFlow);
			return costs[i];
		}
	}

}
