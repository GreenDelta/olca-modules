package org.openlca.core.results.providers.libblocks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.results.providers.InversionResult;
import org.openlca.core.results.providers.InversionResultProvider;
import org.openlca.core.results.providers.LibraryCache;
import org.openlca.core.results.providers.LibImpactMatrix;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.SolverContext;

public class LibBlockSolver {

	private final SolverContext context;
	private final MatrixSolver solver;
	private final LibraryCache libs;

	public static ResultProvider solve(SolverContext context) {
		return new LibBlockSolver(context).solve();
	}

	private LibBlockSolver(SolverContext context) {
		this.context = context;
		this.solver = context.solver();
		this.libs = context.libraries();
	}

	private ResultProvider solve() {
		var f = context.data();
		var demand = f.demand;

		// if the reference flow comes already from a library, we can
		// construct the result directly from the library matrices
		if (demand.techFlow().isFromLibrary())
			return buildSingleLibraryResult();

		var techIndex = new TechIndex();
		for (var techFlow : f.techIndex) {
			if (techFlow.isFromLibrary())
				continue;
			techIndex.add(techFlow);
		}

		var techIndexF = techIndex.copy();
		var libTechIndices = libs.techIndicesOf(f.techIndex);

		int offset = techIndexF.size();
		var libOffsets = new HashMap<String, Integer>();
		for (var e : libTechIndices.entrySet()) {
			var lib = e.getKey();
			var libIdx = e.getValue();
			techIndex.addAll(libIdx);
			libOffsets.put(lib, offset);
			offset += libIdx.size();
		}

		var n = techIndex.size();
		var sparse = isSparse(libTechIndices);
		var techMatrix = sparse
			? new HashPointMatrix(n, n)
			: new DenseMatrix(n, n);
		var inverse = new DenseMatrix(n, n);

		int[] map = f.techIndex.mapTo(techIndex);
		f.techMatrix.iterate((row, col, value) -> {
			var colFlow = f.techIndex.at(col);
			if (colFlow.isFromLibrary())
				return;
			techMatrix.set(map[row], map[col], value);
		});
		var techMatrixF = new DenseMatrix(techIndexF.size(), techIndexF.size());
		techMatrix.copyTo(techMatrixF);
		var inverseF = solver.invert(techMatrixF);
		inverseF.copyTo(inverse);

		for (var lib : libOffsets.keySet()) {
			var libTechMatrix = libs.matrixOf(lib, LibMatrix.A);
			var libOffset = libOffsets.get(lib);
			if (libTechMatrix == null || libOffset == null)
				continue;
			libTechMatrix.copyTo(techMatrix, libOffset, libOffset);
			var libInverse = libs.matrixOf(lib, LibMatrix.INV);
			if (libInverse == null) {
				libInverse = solver.invert(libTechMatrix);
			}
			libInverse.copyTo(inverse, libOffset, libOffset);

			var c = Range.of(
					libOffset, libTechMatrix.rows(), 0, techIndexF.size())
				.slice(techMatrix);
			var y = solver.multiply(libInverse, solver.multiply(c, inverseF));
			negate(y)				;
			y.copyTo(inverse, libOffset, 0);
		}

		var libEnviIndices = new HashMap<String, EnviIndex>();
		EnviIndex firstEnviIndex = null;
		String firstEnviLib = null;
		for (var lib : libTechIndices.keySet()) {
			var libEnviIndex = libs.enviIndexOf(lib);
			if (libEnviIndex == null)
				continue;
			libEnviIndices.put(lib, libEnviIndex);
			if (firstEnviIndex == null
				|| firstEnviIndex.size() < libEnviIndex.size()) {
				firstEnviIndex = libEnviIndex;
				firstEnviLib = lib;
			}
		}

		var enviIndex = firstEnviIndex != null
			? firstEnviIndex.copy()
			: f.enviIndex != null
			? f.enviIndex.copy()
			: null;
		if (enviIndex != null) {
			if (firstEnviLib != null && f.enviIndex != null) {
				enviIndex.addAll(f.enviIndex);
			}

		}


		var data = new MatrixData();
		data.techMatrix = techMatrix;
		data.techIndex = techIndex;


		var result = InversionResult.of(context.solver(), data)
			.withInverse(inverse)
			// .withInventoryIntensities(intensities)
			.calculate();
		return InversionResultProvider.of(result);
	}

	private boolean isSparse(Map<String, TechIndex> indices) {
		double entries = 0;
		double total = 0;
		for (var e : indices.entrySet()) {
			var lib = e.getKey();
			var index = e.getValue();
			var libDir = libs.dir()
				.getLibrary(lib)
				.map(Library::folder)
				.orElse(null);
			if (libDir == null)
				continue;
			total += index.size();
			var sparse = new File(libDir, "A.npz").exists();
			var f = sparse ? 0.25 : 0.75;
			entries += f * Math.pow(index.size(), 2.0);
		}
		return entries == 0
			|| total == 0
			|| (entries / Math.pow(total, 2.0)) < 0.4;
	}

	private void negate(Matrix matrix) {
		for (int col = 0; col < matrix.columns(); col++) {
			for (int row = 0; row < matrix.rows(); row++) {
				var val = matrix.get(row, col);
				if (val != 0) {
					matrix.set(row, col, -val);
				}
			}
		}
	}


	private ResultProvider buildSingleLibraryResult() {

		var f = context.data();
		var demand = context.demand();
		var lib = demand.techFlow().library();

		var data = new MatrixData();
		data.demand = demand;
		data.techIndex = libs.techIndexOf(lib);
		data.techMatrix = libs.matrixOf(lib, LibMatrix.A);
		data.enviIndex = libs.enviIndexOf(lib);
		data.enviMatrix = libs.matrixOf(lib, LibMatrix.B);
		data.costVector = libs.costsOf(lib).orElse(null);

		// not that the LCIA data can be in another library or in the database
		if (MatrixIndex.isPresent(data.enviIndex)
			&& MatrixIndex.isPresent(f.impactIndex)) {
			data.impactMatrix = LibImpactMatrix.of(f.impactIndex, data.enviIndex)
				.withLibraryEnviIndices(Map.of(lib, data.enviIndex))
				.build(context.db(), libs.dir());
			data.impactIndex = f.impactIndex;
		}

		var result = InversionResult.of(context.solver(), data)
			.withInverse(libs.matrixOf(lib, LibMatrix.INV))
			.withInventoryIntensities(libs.matrixOf(lib, LibMatrix.M))
			.calculate();
		return InversionResultProvider.of(result);
	}

	private record Range(int row, int rowLen, int col, int colLen) {

		static Range of(int row, int rowLen, int col, int colLen) {
			return new Range(row, rowLen, col, colLen);
		}

		Matrix slice(MatrixReader matrix) {
			var m = matrix.isSparse()
				? new HashPointMatrix(rowLen, colLen)
				: new DenseMatrix(rowLen, colLen);
			for (int c = 0; c < colLen; c++) {
				for (int r = 0; r < rowLen; r++) {
					m.set(r, c, matrix.get(r + row, c + col));
				}
			}
			return m;
		}
	}

	private class BlockEnviIndex {

		BlockEnviIndex() {

		}
	}

}
