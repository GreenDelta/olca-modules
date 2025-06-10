package org.openlca.core.results.providers.libblocks;

import java.util.function.BiConsumer;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.MatrixIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.results.providers.InversionResult;
import org.openlca.core.results.providers.InversionResultProvider;
import org.openlca.core.results.providers.LibImpactMatrix;
import org.openlca.core.results.providers.ResultProvider;
import org.openlca.core.results.providers.SolverContext;
import org.openlca.core.results.providers.libblocks.BlockTechIndex.Block;

public class LibraryInversionSolver {

	private final SolverContext context;
	private final MatrixSolver solver;
	private final LibReaderRegistry libs;

	private final BlockTechIndex techIdx;
	private final BlockEnviIndex enviIdx;
	private final Matrix techMatrix;
	private final DenseMatrix inverse;
	private final Matrix enviMatrix;
	private final DenseMatrix intensities;

	public static ResultProvider solve(SolverContext context) {
		var demand = context.data().demand;
		// if the reference flow comes already from a library, we can
		// construct the result directly from the library matrices
		return context.libraries().dataPackages.isLibrary(demand.techFlow().dataPackage())
			? SingleLibraryResult.of(context)
			: new LibraryInversionSolver(context).solve();
	}

	private LibraryInversionSolver(SolverContext context) {
		this.context = context;
		this.solver = context.solver();
		this.libs = context.libraries();
		techIdx = BlockTechIndex.of(context);
		enviIdx = BlockEnviIndex.of(context, techIdx);

		int n = techIdx.size();
		techMatrix = techIdx.isSparse
				? new HashPointMatrix(n, n)
				: new DenseMatrix(n, n);
		inverse = new DenseMatrix(n, n);

		if (enviIdx.isEmpty()) {
			enviMatrix = null;
			intensities = null;
		} else {
			int m = enviIdx.size();
			enviMatrix = techIdx.isSparse
					? new HashPointMatrix(m, n)
					: new DenseMatrix(m, n);
			intensities = new DenseMatrix(m, n);
		}
	}

	private ResultProvider solve() {

		// TODO: create a cost vector if costs are present
		var topInv = createTopInv();

		for (var block : techIdx.blocks) {
			int offset = block.offset();
			var lib = block.reader();

			// fill blocks of matrices A and INV
			var libA = lib.matrixOf(LibMatrix.A);
			if (libA == null)
				continue;
			libA.copyTo(techMatrix, offset, offset);
			var libInv = lib.matrixOf(LibMatrix.INV);
			if (libInv == null) {
				libInv = solver.invert(libA);
			}
			libInv.copyTo(inverse, offset, offset);

			addLinkInversionOf(block, topInv, libInv);
			addInterventionsOf(block, libInv);
		}

		addForegroundInterventions();

		var data = new MatrixData();
		data.demand = context.demand();
		data.techMatrix = techMatrix;
		data.techIndex = techIdx.index;
		data.enviIndex = enviIdx.index;
		data.enviMatrix = enviMatrix;
		data.costVector = loadCosts();

		// add LCIA data
		var f = context.data();
		if (MatrixIndex.isPresent(data.enviIndex)
				&& MatrixIndex.isPresent(f.impactIndex)) {
			data.impactIndex = f.impactIndex;
			data.impactMatrix = LibImpactMatrix.of(f.impactIndex, data.enviIndex)
					.build(context.db(), libs);
		}

		var result = InversionResult.of(context.solver(), data)
				.withInverse(inverse)
				.withFlowIntensities(intensities)
				.calculate();
		return InversionResultProvider.of(result);
	}

	private void addInterventionsOf(Block block, MatrixReader libInv) {
		if (enviMatrix == null || !enviIdx.contains(block.id()))
			return;
		var lib = block.reader();
		var libB = lib.matrixOf(LibMatrix.B);
		if (libB == null)
			return;
		var libM = lib.matrixOf(LibMatrix.M);
		if (libM == null) {
			libM = solver.multiply(libB, libInv);
		}

		var offset = block.offset();
		if (enviIdx.isFront(block.id())) {
			libB.copyTo(enviMatrix, 0, offset);
			libM.copyTo(intensities, 0, offset);
		} else {
			int[] rowMap = enviIdx.map(block.id());
			libB.iterate((row, col, value) ->
					enviMatrix.set(rowMap[row], col + offset, value));
			libM.iterate((row, col, value) ->
					intensities.set(rowMap[row], col + offset, value));
		}
	}

	private void addLinkInversionOf(
			Block block, Matrix topInv, MatrixReader libInv) {
		int rows = libInv.rows();
		int cols = topInv.columns();
		var links = techMatrix.isSparse()
				? new HashPointMatrix(rows, cols)
				: new DenseMatrix(rows, cols);
		for (int c = 0; c < cols; c++) {
			for (int r = 0; r < rows; r++) {
				double value = techMatrix.get(r + block.offset(), c);
				links.set(r, c, value);
			}
		}
		var inv = solver.multiply(libInv, solver.multiply(links, topInv));
		negate(inv);
		inv.copyTo(inverse, block.offset(), 0);
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

	/**
	 * Fills the top-left corner of the matrices A and INV with the corresponding
	 * foreground block. Returns that top-left block of the inverse.
	 */
	private Matrix createTopInv() {
		var f = context.data();
		int[] map = f.techIndex.mapTo(techIdx.index);
		f.techMatrix.iterate((row, col, value) -> {
			var colFlow = f.techIndex.at(col);
			if (libs.dataPackages.isLibrary(colFlow.dataPackage()))
				return;
			techMatrix.set(map[row], map[col], value);
		});
		var blockA = new DenseMatrix(
				techIdx.front.size(), techIdx.front.size());
		techMatrix.copyTo(blockA);
		var topInv = solver.invert(blockA);
		topInv.copyTo(inverse);
		return topInv;
	}

	private void addForegroundInterventions() {
		var f = context.data();
		if (intensities == null)
			return;

		// add the interventions of the foreground system
		if (MatrixIndex.isPresent(f.enviIndex) && f.enviMatrix != null) {
			int[] rowMap = f.enviIndex.mapTo(enviIdx.index);
			int[] colMap = f.techIndex.mapTo(techIdx.index);
			f.enviMatrix.iterate((row, col, value) -> {
				var colFlow = f.techIndex.at(col);
				if (libs.dataPackages.isLibrary(colFlow.dataPackage()))
					return;
				enviMatrix.set(rowMap[row], colMap[col], value);
			});
		}

		var leftInv = new DenseMatrix(inverse.rows, techIdx.front.size());
		inverse.copyTo(leftInv);
		var leftIntensities = solver.multiply(enviMatrix, leftInv);
		leftIntensities.copyTo(intensities);
	}

	private double[] loadCosts() {
		var d = context.data();
		if (d.costVector == null)
			return null;

		var idx = techIdx.index;
		var costs = new double[techIdx.size()];
		BiConsumer<TechIndex, double[]> mapFn = (blockIdx, blockCosts) -> {
			if (blockIdx == null || blockCosts == null)
				return;
			for (int bi = 0; bi < blockIdx.size(); bi++) {
				var techFlow = blockIdx.at(bi);
				var i = idx.of(techFlow);
				if (i < 0)
					continue;
				costs[i] = blockCosts[bi];
			}
		};

		mapFn.accept(techIdx.front, d.costVector);
		for (var block : techIdx.blocks) {
			var r = block.reader();
			if (!r.hasCostData())
				continue;
			mapFn.accept(r.techIndex(), r.costs());
		}
		return costs;
	}
}
