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

	/// The maximum fill rate after which we switch from sparse to dense blocks.
	private final double maxSparseFileRate;

	/// The number of updates after which we check the current fill rate.
	private final int checkpoint;

	/// The current number of updates that were made.
	private int updates = 0;

	private final HashPointMatrix sparse = new HashPointMatrix();
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

	public boolean isEmpty() {
		return dense == null && sparse.isEmpty();
	}

	public int rows() {
		return Math.max(sparse.rows, denseRows);
	}

	public int columns() {
		return Math.max(sparse.cols, denseCols);
	}

	/// Set the cell (row, col) to the given value.
	public void set(int row, int col, double val) {
		if (row < 0 || col < 0)
			return;
		if (row < denseRows && col < denseCols) {
			dense.set(row, col, val);
			return;
		}
		sparse.set(row, col, val);
		checkFillRate();
	}

	public double get(int row, int col) {
		if (row < 0 || col < 0)
			return 0;
		return row < denseRows && col < denseCols
				? dense.get(row, col)
				: sparse.get(row, col);
	}

	/// Adjusts the value of the cell (row, column) so that it contains the sum of
	/// the given value `v` and the current value (maybe 0) of that cell:
	/// `cell[row, column] += v`
	public void add(int row, int col, double w) {
		if (w == 0 || row < 0 || col < 0)
			return;
		if (row < denseRows && col < denseCols) {
			dense.set(row, col, dense.get(row, col) + w);
			return;
		}
		sparse.add(row, col, w);
		checkFillRate();
	}

	private void checkFillRate() {
		updates++;
		if (updates % checkpoint == 0) {
			double nnz = sparse.getNumberOfEntries();
			// double casts to avoid integer overflows
			double n = (double) sparse.rows * (double) sparse.cols
					- (double) denseRows * (double) denseCols;
			if (n <= 0)
				return;
			double fillRate = nnz / n;
			if (fillRate > maxSparseFileRate) {
				mapDense();
			}
		}
	}

	public Matrix finish() {
		if (dense != null) {
			mapDense();
			log.trace("Finish matrix builder with "
					+ "dense {}*{} matrix", denseRows, denseCols);
			return dense;
		}

		// calculate the fill-rate
		// double casts to avoid integer overflows
		double n = (double) sparse.rows * (double) sparse.cols;
		if (n <= 0)	return sparse;
		double fillRate = sparse.getNumberOfEntries() / n;
		log.trace("Fill rate = {}", fr);

		if (fillRate > maxSparseFileRate) {
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
		// check if we need to allocate a new dense block
		if (dense == null) {
			dense = new DenseMatrix(sparse.rows, sparse.cols);
		} else if (dense.rows < sparse.rows || dense.columns < sparse.cols) {

			// allocate a larger dense block
			int nextRows = Math.max(dense.rows, sparse.rows);
			int nextCols = Math.max(dense.columns, sparse.cols);
			var next = new DenseMatrix(nextRows, nextCols);

			// copy the content of the current dense block to it
			for (int col = 0; col < dense.columns; col++) {
				int oldIdx = col * dense.rows;
				int nextIdx = col * next.rows;
				System.arraycopy(dense.data, oldIdx, next.data, nextIdx, dense.rows);
			}
			dense = next;
		}

		// copy the values from the sparse area to the dense block and
		// clear the sparse area then
		denseRows = dense.rows;
		denseCols = dense.columns;
		log.trace("Copy sparse values to a {}*{} dense block",
				denseRows, denseCols);
		sparse.iterate(
				(row, col, val) -> dense.set(row, col, val));
		sparse.clear();
	}
}
