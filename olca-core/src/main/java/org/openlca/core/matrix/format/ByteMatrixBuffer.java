package org.openlca.core.matrix.format;

public class ByteMatrixBuffer {

	private final double maxSparseFileRate;
	private final int checkpoint;

	private int sparseEntries = 0;
	private final HashPointByteMatrix sparse = new HashPointByteMatrix();
	private DenseByteMatrix dense;
	private int denseCols;
	private int denseRows;

	public ByteMatrixBuffer(double maxSparseFileRate, int checkpoint) {
		this.maxSparseFileRate = maxSparseFileRate;
		this.checkpoint = checkpoint;
	}

	public ByteMatrixBuffer(double maxSparseFileRate) {
		this(maxSparseFileRate, 10_000);
	}

	public ByteMatrixBuffer() {
		this(0.4);
	}

	public boolean isEmpty() {
		return dense == null && sparse.isEmpty();
	}

	public void minSize(int rows, int cols) {
		if (sparse.rows < rows) {
			sparse.rows = rows;
		}
		if (sparse.columns < cols) {
			sparse.columns = cols;
		}
	}

	public void set(int row, int col, byte val) {
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
			double n = (double) sparse.rows * (double) sparse.columns
				- (double) denseRows * (double) denseCols;
			double fr = (double) sparseEntries / n;
			if (fr > maxSparseFileRate) {
				mapDense();
			}
		}
	}

	public ByteMatrix finish() {
		if (dense != null) {
			mapDense();
			return dense;
		}
		double n = (double) sparse.rows * (double) sparse.columns;
		double fr = (double) sparseEntries / n;
		if (fr > maxSparseFileRate) {
			mapDense();
			return dense;
		}
		return sparse;
	}

	private void mapDense() {
		if (dense == null) {
			dense = new DenseByteMatrix(sparse.rows, sparse.columns);
		} else if (dense.rows < sparse.rows
			|| dense.columns < sparse.columns){
			var next = new DenseByteMatrix(sparse.rows, sparse.columns);
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
		sparse.iterate(dense::set);
		sparse.clear();
		sparseEntries = 0;
	}

}
