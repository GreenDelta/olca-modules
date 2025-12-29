package org.openlca.core.matrix.format;

import org.openlca.core.matrix.solvers.accelerate.AccelerateSparseFactorization;

/**
 * A sparse matrix format optimized for Apple Accelerate framework.
 * This format stores data in CSC (Compressed Sparse Column) format
 * which matches Accelerate's SparseMatrix_Double structure directly.
 * 
 * Unlike CSCMatrix, this class is specifically designed for Accelerate
 * operations and can be used directly without conversion overhead.
 * 
 * The format matches Accelerate's expected structure:
 * - columnStarts: array of length (columns + 1) indicating where each column starts
 * - rowIndices: array of row indices for each non-zero value
 * - values: array of non-zero values
 * 
 * This is the same as CSC format, but explicitly optimized for Accelerate usage.
 */
public final class AccelerateSparseMatrix implements MatrixReader {

	public final int rows;
	public final int columns;

	/**
	 * Column start indices (CSC format). Length must be columns + 1.
	 * columnStarts[i] indicates the start index in rowIndices/values for column i.
	 * columnStarts[columns] equals the total number of non-zeros.
	 */
	public final int[] columnStarts;

	/**
	 * Row indices for each non-zero value (CSC format).
	 * Length equals the number of non-zeros.
	 */
	public final int[] rowIndices;

	/**
	 * Non-zero values (CSC format).
	 * Length equals the number of non-zeros.
	 */
	public final double[] values;

	private AccelerateSparseMatrix(int rows, int columns,
			int[] columnStarts,
			int[] rowIndices,
			double[] values) {
		this.rows = rows;
		this.columns = columns;
		this.columnStarts = columnStarts;
		this.rowIndices = rowIndices;
		this.values = values;
	}

	/**
	 * Creates from CSC format arrays (direct match to Accelerate format).
	 * 
	 * @param rows number of rows
	 * @param columns number of columns
	 * @param columnStarts column start indices (length = columns + 1)
	 * @param rowIndices row indices for each non-zero (length = nnz)
	 * @param values non-zero values (length = nnz)
	 */
	public static AccelerateSparseMatrix of(int rows, int columns,
			int[] columnStarts,
			int[] rowIndices,
			double[] values) {
		// Validate
		if (rows <= 0 || columns <= 0) {
			throw new IllegalArgumentException("Rows and columns must be positive");
		}
		if (columnStarts == null || columnStarts.length != columns + 1) {
			throw new IllegalArgumentException(
					"columnStarts length must be columns + 1");
		}
		if (rowIndices == null || values == null) {
			throw new IllegalArgumentException(
					"rowIndices and values cannot be null");
		}
		if (rowIndices.length != values.length) {
			throw new IllegalArgumentException(
					"rowIndices and values must have same length");
		}
		if (columnStarts[columns] != values.length) {
			throw new IllegalArgumentException(
					"columnStarts[columns] must equal number of non-zeros");
		}
		if (columnStarts[0] != 0) {
			throw new IllegalArgumentException(
					"columnStarts[0] must be 0");
		}

		// Validate column starts are non-decreasing
		for (int i = 0; i < columns; i++) {
			if (columnStarts[i] > columnStarts[i + 1]) {
				throw new IllegalArgumentException(
						"columnStarts must be non-decreasing");
			}
		}

		// Validate row indices are within bounds
		for (int i = 0; i < rowIndices.length; i++) {
			if (rowIndices[i] < 0 || rowIndices[i] >= rows) {
				throw new IllegalArgumentException(
						"Row index out of bounds: " + rowIndices[i]);
			}
		}

		return new AccelerateSparseMatrix(rows, columns,
				columnStarts.clone(),
				rowIndices.clone(),
				values.clone());
	}

	/**
	 * Creates from an existing CSCMatrix.
	 */
	public static AccelerateSparseMatrix of(CSCMatrix csc) {
		return new AccelerateSparseMatrix(
				csc.rows,
				csc.columns,
				csc.columnPointers.clone(),
				csc.rowIndices.clone(),
				csc.values.clone());
	}

	/**
	 * Creates from a HashPointMatrix by compressing it.
	 */
	public static AccelerateSparseMatrix of(HashPointMatrix hpm) {
		CSCMatrix csc = hpm.compress();
		return of(csc);
	}

	/**
	 * Creates from any MatrixReader by converting to CSC first.
	 */
	public static AccelerateSparseMatrix of(MatrixReader m) {
		if (m == null) {
			throw new NullPointerException("Matrix cannot be null");
		}
		if (m instanceof AccelerateSparseMatrix) {
			return (AccelerateSparseMatrix) m;
		}
		if (m instanceof CSCMatrix) {
			return of((CSCMatrix) m);
		}
		if (m instanceof HashPointMatrix) {
			return of((HashPointMatrix) m);
		}
		// Convert to CSC first
		CSCMatrix csc = CSCMatrix.of(m);
		return of(csc);
	}

	@Override
	public boolean isSparse() {
		return true;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public double get(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= columns) {
			throw new IndexOutOfBoundsException(
					"Index out of bounds: (" + row + ", " + col + ")");
		}
		int start = columnStarts[col];
		int end = col < (columns - 1)
				? columnStarts[col + 1]
				: rowIndices.length;
		for (int i = start; i < end; i++) {
			if (rowIndices[i] == row) {
				return values[i];
			}
		}
		return 0;
	}

	@Override
	public double[] getColumn(int col) {
		if (col < 0 || col >= columns) {
			throw new IndexOutOfBoundsException("Column index out of bounds: " + col);
		}
		double[] v = new double[rows];
		int start = columnStarts[col];
		int end = col < (columns - 1)
				? columnStarts[col + 1]
				: rowIndices.length;
		for (int i = start; i < end; i++) {
			v[rowIndices[i]] = values[i];
		}
		return v;
	}

	@Override
	public double[] getRow(int row) {
		if (row < 0 || row >= rows) {
			throw new IndexOutOfBoundsException("Row index out of bounds: " + row);
		}
		double[] v = new double[columns];
		for (int col = 0; col < columns; col++) {
			v[col] = get(row, col);
		}
		return v;
	}

	@Override
	public void iterate(EntryFunction fn) {
		if (fn == null)
			return;
		for (int col = 0; col < columns; col++) {
			int start = columnStarts[col];
			int end = col < (columns - 1)
					? columnStarts[col + 1]
					: rowIndices.length;
			for (int i = start; i < end; i++) {
				fn.value(rowIndices[i], col, values[i]);
			}
		}
	}

	@Override
	public MatrixReader copy() {
		return of(this);
	}

	@Override
	public void readColumn(int column, double[] buffer) {
		if (column < 0 || column >= columns) {
			throw new IndexOutOfBoundsException("Column index out of bounds: " + column);
		}
		int n = Math.min(buffer.length, rows);
		java.util.Arrays.fill(buffer, 0);
		int start = columnStarts[column];
		int end = column < (columns - 1)
				? columnStarts[column + 1]
				: rowIndices.length;
		for (int i = start; i < end; i++) {
			int row = rowIndices[i];
			if (row < n) {
				buffer[row] = values[i];
			}
		}
	}

	/**
	 * Gets the number of non-zero entries.
	 */
	public int getNonZeroCount() {
		return values.length;
	}

	/**
	 * Converts to CSCMatrix. This is a zero-copy operation in terms of
	 * data structure (both use CSC format), but creates a new object.
	 */
	public CSCMatrix toCSC() {
		return new CSCMatrix(rows, columns, values, columnStarts, rowIndices);
	}

	/**
	 * Creates a sparse factorization directly using Accelerate.
	 * This avoids conversion overhead since the data is already in
	 * the correct format for Accelerate.
	 */
	public AccelerateSparseFactorization factorize() {
		return AccelerateSparseFactorization.of(this);
	}
}

