package org.openlca.core.results.providers;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibMatrix;
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
import gnu.trove.map.hash.TIntObjectHashMap;

public class LazyLibraryProvider implements ResultProvider {

	private final IDatabase db;
	private final LibCache libs;
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

	private LazyLibraryProvider(SolverContext context) {
		this.db = context.db();
		this.libs = LibCache.of(context);
		this.solver = context.solver();
		this.demand = context.demand();
		this.foregroundData = context.data();
		this.foregroundSolution = EagerResultProvider.create(context);
		this.fullData = new MatrixData();
		this.fullData.demand = demand;
		this.fullData.impactIndex = foregroundData.impactIndex;
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

	public static LazyLibraryProvider of(SolverContext context) {

		var provider = new LazyLibraryProvider(context);
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

		// initialize the combined index with the index
		// of the foreground system
		var demand = foregroundData.demand;
		var foregroundTechIndex = foregroundData.techIndex;
		var techIndex = new TechIndex(demand.techFlow());
		var libQueue = new ArrayDeque<String>();
		for (var techFlow : foregroundTechIndex) {
			techIndex.add(techFlow);
			var lib = techFlow.library();
			if(lib != null && !libQueue.contains(lib)) {
				libQueue.add(lib);
			}
		}

		// recursively add the indices of the used libraries
		while (!libQueue.isEmpty()) {
			var libId = libQueue.poll();
			usedLibs.add(libId);
			var libIndex = libs.techIndexOf(libId);
			if (libIndex == null)
				continue;
			for (var techFlow : libIndex) {
				techIndex.add(techFlow);
				var nextLibId = techFlow.library();
				if (nextLibId == null
					|| libId.equals(nextLibId)
					|| usedLibs.contains(nextLibId)
					|| libQueue.contains(nextLibId))
					continue;
				libQueue.add(nextLibId);
			}
		}

		// initialize the flow index with the foreground
		// index if present
		EnviIndex enviIndex = null;
		if (foregroundData.enviIndex != null) {
			enviIndex = foregroundData.enviIndex.isRegionalized()
				? EnviIndex.createRegionalized()
				: EnviIndex.create();
			enviIndex.addAll(foregroundData.enviIndex);
		}
		// extend the flow index with the flow indices
		// of used libraries.
		for (var libId : usedLibs) {
			var libIdx =libs.enviIndexOf(libId);
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
	public EnviIndex flowIndex() {
		return fullData.enviIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return fullData.impactIndex;
	}

	@Override
	public boolean hasCosts() {
		// TODO: not yet implemented
		return false;
	}

	@Override
	public double[] scalingVector() {
		return scalingVector;
	}

	@Override
	public double scalingFactorOf(int product) {
		return scalingVector[product];
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
			if (product.isFromLibrary())
				continue;
			t[i] = techF.get(i, i) * scalingVector[i];
		}

		// handle the libraries
		for (var libId : usedLibs) {
			var indexB = libs.techIndexOf(libId);
			if (indexB == null)
				continue;
			var libDiag = libs.diagonalOf(libId, LibMatrix.A);
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
	public double totalRequirementsOf(int product) {
		var t = totalRequirements();
		return t[product];
	}

	@Override
	public double[] techColumnOf(int j) {
		var column = techColumns.get(j);
		if (column != null)
			return column;

		var index = fullData.techIndex;
		var product = index.at(j);
		column = new double[index.size()];
		var libID = product.library();

		// in case of a foreground product, we just need
		// to copy the column of the foreground system
		// into the first part of the result column as
		// the tech. index of the foreground system is
		// exactly the first part of the combined index
		if (libID == null) {
			var colF = foregroundData.techMatrix.getColumn(j);
			System.arraycopy(colF, 0, column, 0, colF.length);
			return put(j, techColumns, column);
		}

		// in case of a library product, we need to map
		// the column entries
		var indexLib = libs.techIndexOf(libID);
		if (indexLib == null)
			return column;
		int jLib = indexLib.of(product);
		var columnLib = libs.columnOf(libID, LibMatrix.A, jLib);
		if (columnLib == null)
			return column;
		for (int iLib = 0; iLib < columnLib.length; iLib++) {
			double val = columnLib[iLib];
			if (val == 0)
				continue;
			var providerLib = indexLib.at(iLib);
			var i = index.of(providerLib);
			if (i < 0)
				continue;
			column[i] = val;
		}
		return put(j, techColumns, column);
	}

	@Override
	public double[] solutionOfOne(int product) {
		var solution = solutions.get(product);
		if (solution != null)
			return solution;

		var techIndex = fullData.techIndex;
		solution = new double[techIndex.size()];

		// initialize a queue that is used for adding scaled
		// sub-solutions of libraries recursively
		var queue = new ArrayDeque<Pair<TechFlow, Double>>();
		var start = fullData.techIndex.at(product);
		if (start.isFromLibrary()) {
			// start process is a library process
			queue.push(Pair.of(start, 1.0));
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
				if (provider.isFromLibrary()) {
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
			double factor = pair.second;
			var libId = p.library();
			if (libId == null)
				continue;
			var libIndex = libs.techIndexOf(libId);
			if (libIndex == null)
				continue;
			int column = libIndex.of(p);
			var libSolution = libs.columnOf(libId,LibMatrix.INV, column);
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

		return put(product, solutions, solution);
	}

	@Override
	public double loopFactorOf(int product) {
		var aii = techValueOf(product, product);
		var ii = solutionOfOne(product)[product];
		var f = aii * ii;
		return f == 0
				? 0
				: 1 / f;
	}

	@Override
	public double[] unscaledFlowsOf(int j) {
		var column = flowColumns.get(j);
		if (column != null)
			return column;

		var flowIdx = fullData.enviIndex;
		if (flowIdx == null)
			return EMPTY_VECTOR;

		column = new double[flowIdx.size()];
		var product = fullData.techIndex.at(j);
		var libId = product.library();

		// in case of a foreground product, we just need
		// to copy the column of the foreground system
		// into the first part of the result column as
		// the flow index of the foreground system is
		// exactly the first part of the combined index
		if (libId == null) {
			var flowMatrixF = foregroundData.enviMatrix;
			if (flowMatrixF != null) {
				var colF = flowMatrixF.getColumn(j);
				System.arraycopy(colF, 0, column, 0, colF.length);
			}
			return put(j, flowColumns, column);
		}

		// in case of a library product, we need to map
		// the column entries
		var flowIdxB = libs.enviIndexOf(libId);
		var techIdxB = libs.techIndexOf(libId);
		if (flowIdxB == null || techIdxB == null)
			return put(j, flowColumns, column);
		var jB = techIdxB.of(product);
		var colB = libs.columnOf(libId, LibMatrix.B, jB);
		if (colB == null)
			return put(j, flowColumns, column);

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

		return put(j, flowColumns, column);
	}

	@Override
	public double unscaledFlowOf(int flow, int product) {
		var flows = unscaledFlowsOf(product);
		return isEmpty(flows)
				? 0
				: flows[flow];
	}

	@Override
	public double[] directFlowsOf(int product) {
		var flows = directFlows.get(product);
		if (flows != null)
			return flows;
		var unscaled = unscaledFlowsOf(product);
		if (isEmpty(unscaled))
			return EMPTY_VECTOR;
		var factor = scalingFactorOf(product);
		flows = scale(unscaled, factor);
		return put(product, directFlows, flows);
	}

	@Override
	public double directFlowOf(int flow, int product) {
		var flows = directFlowsOf(product);
		return isEmpty(flows)
				? 0
				: flows[flow];
	}

	@Override
	public double[] totalFlowsOfOne(int j) {
		var totals = totalFlowsOfOne.get(j);
		if (totals != null)
			return totals;

		var flowIndex = fullData.enviIndex;
		if (flowIndex == null || flowIndex.size() == 0) {
			return EMPTY_VECTOR;
		}

		var s = solutionOfOne(j);
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
			var flowIdxB = libs.enviIndexOf(libId);
			var techIdxB = libs.techIndexOf(libId);
			if (flowIdxB == null || techIdxB == null)
				continue;
			if (flowIdxB.size() == 0)
				continue;

			// calculate the scaled library result
			var matrixB = libs.matrixOf(libId, LibMatrix.B);
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
		return put(j, totalFlowsOfOne, totals);
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
		var flowIndex = flowIndex();
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
			var libFlowIdx = libs.enviIndexOf(libId);
			if (libFlowIdx == null)
				continue;
			var libImpactIdx = libs.impactIndexOf(libId);
			if (libImpactIdx == null)
				continue;
			var libMatrix = libs.matrixOf(libId, LibMatrix.C);
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

		fullData.impactMatrix = builder.finish();
		return fullData.impactMatrix;
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		var matrix = impactFactors();
		return matrix == null
				? EMPTY_VECTOR
				: matrix.getColumn(flow);
	}

	@Override
	public double impactFactorOf(int indicator, int flow) {
		var matrix = impactFactors();
		return matrix == null
				? 0
				: matrix.get(indicator, flow);
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
	public double[] flowImpactsOf(int flow) {
		var impacts = flowImpacts();
		return impacts == null
				? EMPTY_VECTOR
				: impacts.getColumn(flow);
	}

	@Override
	public double flowImpactOf(int indicator, int flow) {
		var impacts = flowImpacts();
		return impacts == null
				? 0
				: impacts.get(indicator, flow);
	}

	@Override
	public double[] directImpactsOf(int product) {
		var impacts = directImpacts.get(product);
		if (impacts != null)
			return impacts;
		var factors = impactFactors();
		var flows = directFlowsOf(product);
		if (factors == null || isEmpty(flows))
			return EMPTY_VECTOR;
		impacts = solver.multiply(factors, flows);
		return put(product, directImpacts, impacts);
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		var impacts = totalImpactsOfOne.get(product);
		if (impacts != null)
			return impacts;
		var factors = impactFactors();
		var flows = totalFlowsOfOne(product);
		if (factors == null || isEmpty(flows))
			return EMPTY_VECTOR;
		impacts = solver.multiply(factors, flows);
		return put(product, totalImpactsOfOne, impacts);
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
	public double directCostsOf(int product) {
		// TODO: not yet implemented
		return 0;
	}

	@Override
	public double totalCostsOfOne(int product) {
		return 0;
	}

	@Override
	public double totalCosts() {
		return 0;
	}
}
