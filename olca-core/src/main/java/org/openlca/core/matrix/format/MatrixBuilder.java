package org.openlca.core.matrix.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatrixBuilder {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final double maxSparseFileRate;
	private final int checkpoint;

	private int sparseEntries = 0;
	private HashPointMatrix sparse = new HashPointMatrix();
	private DenseMatrix dense;
	private int denseCols; // we need these because
	private int denseRows; // dense is null by default

	public MatrixBuilder() {
		this(0.4, 10_000);
	}

	public MatrixBuilder(double maxSparseFileRate) {
		this(maxSparseFileRate, 10_000);
	}

	public MatrixBuilder(double maxSparseFileRate, int checkpoint) {
		this.maxSparseFileRate = maxSparseFileRate;
		this.checkpoint = checkpoint;
	}

	public void minSize(int rows, int cols) {
		if (sparse.rows < rows) {
			sparse.rows = rows;
		}
		if (sparse.cols < cols) {
			sparse.cols = cols;
		}
	}

	public void put(int row, int col, double val) {
		if (val == 0 || row < 0 || col < 0)
			return;
		if (row < denseRows && col < denseCols) {
			dense.set(row, col, val);
			return;
		}
		sparse.set(row, col, val);
		sparseEntries++;
		if (sparseEntries % checkpoint == 0) {
			// double casts to avoid integer overflows
			double n = (double) sparse.rows * (double) sparse.cols
					- (double) denseRows * (double) denseCols;
			double fr = (double) sparseEntries / n;
			if (fr > maxSparseFileRate) {
				mapDense();
			}
		}
	}

	public IMatrix finish() {
		if (dense != null) {
			mapDense();
			log.trace("Finish matrix builder with "
					+ "dense {}*{} matrix", denseRows, denseCols);
			return dense;
		}
		// double casts to avoid integer overflows
		double n = (double) sparse.rows * (double) sparse.cols;
		double fr = (double) sparseEntries / n;
		log.trace("Fill rate = {}", fr);
		if (fr > maxSparseFileRate) {
			mapDense();
			log.trace("Finish matrix builder with "
					+ "dense {}*{} matrix", denseRows, denseCols);
			return dense;
		}
		log.trace("Finish matrix builder with "
				+ "sparse {}*{} matrix", sparse.rows, sparse.cols);
		return sparse;
	}

	private void mapDense() {
		if (dense == null) {
			dense = new DenseMatrix(
					sparse.rows, sparse.cols);
		} else if (dense.rows < sparse.rows
				|| dense.columns < sparse.cols) {
			DenseMatrix next = new DenseMatrix(
					sparse.rows, sparse.cols);
			for (int col = 0; col < dense.columns; col++) {
				int oldIdx = col * dense.rows;
				int nextIdx = col * next.rows;
				System.arraycopy(dense.data, oldIdx,
						next.data, nextIdx, dense.rows);
			}
			dense = next;
		}
		denseRows = dense.rows;
		denseCols = dense.columns;
		log.trace("Allocated a {}*{} dense matrix; {} new entries",
				denseRows, denseCols, sparseEntries);
		sparse.iterate(
				(row, col, val) -> dense.set(row, col, val));
		sparse.clear();
		sparseEntries = 0;
	}
}
