package org.openlca.core.results.solutions;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Objects;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.library.LibraryMatrix;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.util.Pair;

public class LibrarySolutionProvider implements SolutionProvider {

	private final IDatabase db;
	private final LibraryDir libDir;
	private final IMatrixSolver solver;

	private final MatrixData foregroundData;
	private final SolutionProvider foregroundSolutions;

	// cached results
	private final MatrixData fullData;
	private final TIntObjectHashMap<double[]> solutions;

	// library maps: libID -> T
	private final HashMap<String, Library> libraries = new HashMap<>();
	private final HashMap<String, TechIndex> libTechIndices = new HashMap<>();
	private final HashMap<String, FlowIndex> libFlowIndices = new HashMap<>();

	private LibrarySolutionProvider(
			IDatabase db,
			LibraryDir libDir,
			IMatrixSolver solver,
			MatrixData foregroundData) {
		this.db = db;
		this.libDir = libDir;
		this.solver = solver;
		this.foregroundData = foregroundData;
		this.foregroundSolutions = DenseSolutionProvider.create(
				foregroundData, solver);

		this.fullData = new MatrixData();
		this.solutions = new TIntObjectHashMap<>();
	}

	public static LibrarySolutionProvider of (
			IDatabase db,
			LibraryDir libDir,
			IMatrixSolver solver,
			MatrixData foregroundData) {

		var provider = new LibrarySolutionProvider(
				db, libDir, solver, foregroundData);
		provider.initTechIndex();
		return provider;
	}

	private void initTechIndex() {

		var indexF = foregroundData.techIndex;
		var index = new TechIndex(indexF.getRefFlow());
		index.setDemand(indexF.getDemand());

		indexF.each((pos, product) -> {
			var lib = product.getLibrary();
			if (lib.isEmpty()) {
				index.put(product);
			} else {
				libraries.computeIfAbsent(lib.get(),
						libID -> libDir.get(libID).orElseThrow(
								() -> new RuntimeException(
										"Could not load library " + libID)));

			}
		});

		libraries.keySet().forEach(libID -> {
			var lib = libraries.get(libID);
			var libIndex = lib.syncProducts(db).orElseThrow(
					() -> new RuntimeException(
							"Could not load product index of " + libID));
			libTechIndices.put(libID, libIndex);
			libIndex.each((_pos, product) -> index.put(product));
		});

		fullData.techIndex = index;
	}

	@Override
	public double[] scalingVector() {
		return new double[0];
	}

	@Override
	public double[] columnOfA(int product) {
		return new double[0];
	}

	@Override
	public double valueOfA(int row, int col) {
		return 0;
	}

	@Override
	public double scaledValueOfA(int row, int col) {
		return 0;
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
		var queue = new ArrayDeque<Pair<ProcessProduct, Double>>();
		var start = fullData.techIndex.getProviderAt(product);
		if (start.getLibrary().isPresent()) {
			// start process is a library process
			queue.push(Pair.of(start, 1.0));
		} else {
			// start process is a foreground process
			// we copy the values of the solution of
			// the foreground system or initialize
			// the entries of the queue with the scaled
			// library links
			var idxF = foregroundData.techIndex;
			var pf = idxF.getIndex(start);
			var sf = foregroundSolutions.solutionOfOne(pf);
			for (int i = 0; i < sf.length; i++) {
				var value = sf[i];
				if (value == 0)
					continue;
				var provider = idxF.getProviderAt(i);
				if (provider.getLibrary().isPresent()) {
					queue.push(Pair.of(provider, value));
				} else {
					int index = techIndex.getIndex(provider);
					solution[index] = value;
				}
			}
		}

		// recursively add library solutions
		while (!queue.isEmpty()) {
			var pair = queue.pop();
			var p = pair.first;
			double factor = pair.second;
			var libID = p.getLibrary().orElseThrow();
			var lib = libraries.get(libID);
			var libIndex = libTechIndices.get(libID);
			if (lib == null || libIndex == null)
				continue;
			int column = libIndex.getIndex(p);
			var libSolution = lib.getColumn(
					LibraryMatrix.INV, column)
					.orElse(null);
			if (libSolution == null)
				continue;
			for (int i = 0; i < libSolution.length; i++) {
				var value = libSolution[i];
				if (value == 0)
					continue;
				var provider = libIndex.getProviderAt(i);
				var subLibID = provider.getLibrary().orElse(null);
				if (Objects.equals(libID, subLibID)) {
					int index = techIndex.getIndex(provider);
					solution[index] += factor * value;
				} else {
					queue.push(Pair.of(provider, factor * value));
				}
			}
		}

		solutions.put(product, solution);
		return solution;
	}

	@Override
	public boolean hasFlows() {
		return false;
	}

	@Override
	public double[] totalFlows() {
		return new double[0];
	}

	@Override
	public double[] totalFlowsOfOne(int product) {
		return new double[0];
	}

	@Override
	public double totalFlowOfOne(int flow, int product) {
		return 0;
	}

	@Override
	public boolean hasImpacts() {
		return false;
	}

	@Override
	public double[] totalImpacts() {
		return new double[0];
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		return new double[0];
	}

	@Override
	public double totalImpactOfOne(int indicator, int product) {
		return 0;
	}

	@Override
	public boolean hasCosts() {
		return false;
	}

	@Override
	public double totalCosts() {
		return 0;
	}

	@Override
	public double totalCostsOfOne(int product) {
		return 0;
	}

	@Override
	public double loopFactorOf(int product) {
		return 0;
	}
}
