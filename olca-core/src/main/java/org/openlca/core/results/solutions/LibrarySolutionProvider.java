package org.openlca.core.results.solutions;

import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.IMatrixSolver;

public class LibrarySolutionProvider implements SolutionProvider {

	private final IDatabase db;
	private final LibraryDir libDir;
	private final IMatrixSolver solver;
	private final MatrixData foregroundData;
	private final MatrixData fullData;

	/**
	 * Contains the loaded libraries: library ID -> library.
	 */
	private final HashMap<String, Library> libraries = new HashMap<>();

	/**
	 * The product indices for the loaded libraries.
	 */
	private final HashMap<String, TechIndex> libTechIndices = new HashMap<>();

	/**
	 * The elem. flow indices of the loaded libraries.
	 */
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
		this.fullData = new MatrixData();
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
		return new double[0];
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
