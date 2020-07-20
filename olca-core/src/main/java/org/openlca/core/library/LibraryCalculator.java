package org.openlca.core.library;

import java.util.Arrays;
import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.results.ContributionResult;

public class LibraryCalculator {

	private final IDatabase db;
	private final LibraryDir libDir;
	private final IMatrixSolver solver;

	private MatrixData foregroundData;

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

	public LibraryCalculator(IDatabase db, LibraryDir libDir,
							 IMatrixSolver solver) {
		this.db = db;
		this.libDir = libDir;
		this.solver = solver;
	}

	public ContributionResult calculate(MatrixData foregroundData) {
		this.foregroundData = foregroundData;

		var result = new ContributionResult();
		result.techIndex = techIndex();
		result.flowIndex = flowIndex();
		result.scalingVector = scalingVector(result.techIndex);

		// the total requirements of library products are
		// equal to their scaling factors because their
		// matrices are normalized to 1
		result.totalRequirements = Arrays.copyOf(
				result.scalingVector,
				result.scalingVector.length);
		result.techIndex.each((i, product) -> {
			var lib = product.getLibrary();
			if (lib.isPresent())
				return;
			var ai = foregroundData.techMatrix.get(i, i);
			var si = result.scalingVector[i];
			result.totalRequirements[i] = ai * si;
		});

		addFlowResults(result);

		return result;
	}

	private TechIndex techIndex() {

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

		return index;
	}

	/**
	 * Creates the combined flow index of the library result. Note that
	 * this may be if this is a result without elementary flows.
	 */
	private FlowIndex flowIndex() {

		// initialize the flow index with the foreground
		// index if present
		FlowIndex index = null;
		var indexF = foregroundData.flowIndex;
		if (indexF != null) {
			index = indexF.isRegionalized
					? FlowIndex.createRegionalized()
					: FlowIndex.create();
			index.putAll(indexF);
		}

		// extend the flow index with the flow indices
		// of used libraries.
		for (var entry : libraries.entrySet()) {
			var libID = entry.getKey();
			var lib = entry.getValue();
			var libIdx = lib.syncElementaryFlows(db).orElse(null);
			if (libIdx == null)
				continue;
			if (index == null) {
				index = libIdx.isRegionalized
						? FlowIndex.createRegionalized()
						: FlowIndex.create();
			}
			index.putAll(libIdx);
			libFlowIndices.put(libID, libIdx);
		}

		return index;
	}

	private double[] scalingVector(TechIndex index) {

		// calculate the scaling vector of the foreground system
		var indexF = foregroundData.techIndex;
		var sf = solver.solve(
				foregroundData.techMatrix,
				indexF.getIndex(indexF.getRefFlow()),
				indexF.getDemand());

		var s = new double[index.size()];
		for (int jf = 0; jf < sf.length; jf++) {
			var valF = sf[jf];
			if (valF == 0)
				continue;

			var product = indexF.getProviderAt(jf);
			var j = index.getIndex(product);
			var libID = product.getLibrary().orElse(null);

			// scaling factors of foreground processes are
			// copied into the combined vector
			if (libID == null) {
				s[j] = valF;
				continue;
			}

			// scaling vectors of library products are
			// scaled and mapped to the combined vector
			var libIndex = libTechIndices.get(libID);
			var lib = libraries.get(libID);
			if (libIndex == null || lib == null)
				continue;
			var jLib = libIndex.getIndex(product);
			var sLib = lib.getColumn(LibraryMatrix.INV, jLib)
					.orElse(null);
			if (sLib == null)
				continue;
			for (int iLib = 0; iLib < sLib.length; iLib++) {
				var libProduct = libIndex.getProviderAt(iLib);
				int i = index.getIndex(libProduct);
				s[i] += valF * sLib[iLib];
			}
		}

		return s;
	}

	private void addFlowResults(ContributionResult result) {

		// initialize data structures
		var flowIndex = result.flowIndex;
		var techIndex = result.techIndex;
		if (flowIndex == null)
			return;
		var s = result.scalingVector;
		var g = new double[flowIndex.size()];
		var matrixG = new MatrixBuilder();
		matrixG.minSize(flowIndex.size(), techIndex.size());

		// foreground data
		// TODO we could reuse the scaling vector sf here
		// or maybe even calculate a result for the foreground
		// system
		var flowIndexF = foregroundData.flowIndex;
		var techIndexF = foregroundData.techIndex;
		if (flowIndexF != null) {
			foregroundData.enviMatrix.iterate((rowF, colF, value) -> {

				// map the indices
				var flow = flowIndexF.at(rowF);
				var product = techIndexF.getProviderAt(colF);
				var row = flowIndex.of(flow);
				var col = techIndex.getIndex(product);
				if (row < 0 || col < 0 )
					return;

				var val = s[col] * value;
				if (val == 0)
					return;
				g[row] += val;
				matrixG.add(row, col, val);
			});
		}

		// library data
		for (var e : libraries.entrySet()) {
			var libID = e.getKey();
			var lib = e.getValue();
			var libFlowIndex = libFlowIndices.get(libID);
			if (libFlowIndex == null)
				continue;
			var libTechIndex = libTechIndices.get(libID);
			var libMatrix = lib.getMatrix(LibraryMatrix.B).orElse(null);
			if (libMatrix == null)
				continue;
			libMatrix.iterate((rowLib, colLib, value) -> {

				// map indices
				var flow = libFlowIndex.at(rowLib);
				var product = libTechIndex.getProviderAt(colLib);
				var row = flowIndex.of(flow);
				var col = techIndex.getIndex(product);
				if (row < 0 || col < 0)
					return;

				var val = s[col] * value;
				if (val == 0)
					return;
				g[row] += val;
				matrixG.add(row, col, val);
			});
		}

		result.totalFlowResults = g;
		result.directFlowResults = matrixG.finish();
	}
}
