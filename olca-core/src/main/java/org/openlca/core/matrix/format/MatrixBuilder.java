package org.openlca.core.matrix.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is matrix builder that allows you to create a matrix just by filling the
 * values and which can switch efficiently between a sparse and dense
 * representation during this filling process. It does this by allocation a
 * growing dense matrix block and fast array copying if the fill rate exceeds a
 * specific value. See https://github.com/msrocka/blockm for further
 * information.
 */
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

	/**
	 * Set the cell (row, col) to the given value.
	 */
	public void set(int row, int col, double val) {
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
			double fr = sparseEntries / n;
			if (fr > maxSparseFileRate) {
				mapDense();
			}
		}
	}

	/**
	 * Let $v$ be the current value at the cell $a_{row, col}. This function
	 * adds the given value $w$ to $a_{row, col} so that:
	 *
	 * $$a_{row, col} = v + w$$
	 *
	 * Where $v = 0$ When there is no value at $a_{row, col}.
	 */
	public void add(int row, int col, double w) {
		if (w == 0 || row < 0 || col < 0)
			return;
		double v = 0;
		if (row < denseRows && col < denseCols) {
			v = dense.get(row, col);
		} else {
			v = sparse.get(row, col);
		}
		set(row, col, v + w);
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
		double fr = sparseEntries / n;
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
			dense = new DenseMatrix(sparse.rows, sparse.cols);
		} else if (dense.rows < sparse.rows
				|| dense.columns < sparse.cols) {

			// allocate a new dense block
			int nextRows = Math.max(sparse.rows, dense.rows);
			int nextCols = Math.max(sparse.cols, dense.columns);
			DenseMatrix next = new DenseMatrix(nextRows, nextCols);

			// copy the old dense block into the new one
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
