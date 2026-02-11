package org.openlca.core.matrix.format;

import java.util.Arrays;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/// Implements the compressed-column format for sparse matrices (CSC =
/// compressed sparse column).
public final class CSCMatrix implements SparseMatrixReader {

	/// The total number of rows.
	public final int rows;

	/// The total number of columns.
	public final int columns;

	/// The array with non-zero entries.
	public final double[] values;

	/// The array with the row indices of the non-zero entries, thus it has the
	/// exact same length as the `values` array.
	public final int[] rowIndices;

	/// The array with the column pointers. For each column `j` it contains the
	/// index where the entries of column `j` start in the `values` and
	/// `rowIndices` array. It has `columns + 1` components and the last component
	/// contains the number of non-zero entries (so `values.length`). So for each
	/// column `j` you get the start-index of the values via `columnPointers[j]`
	/// and the exclusive end-index via `columnPointers[j + 1]`. It is important
	/// to exactly follow this definition as we hand over these arrays into math
	/// libraries that expect it like this.
	public final int[] columnPointers;

	public CSCMatrix(int rows, int cols, double[] values,
					 int[] columnPointers, int[] rowIndices) {
		this.rows = rows;
		this.columns = cols;
		this.values = values;
		this.columnPointers = columnPointers;
		this.rowIndices = rowIndices;
	}

	@Override
	public final boolean isSparse() {
		return true;
	}

	/**
	 * Constructs a new matrix from the given values.
	 *
	 * @param values The matrix values as an array of rows (row-major order).
	 */
	public static CSCMatrix of(double[][] values) {
		return HashPointMatrix.of(values).compress();
	}

	/// Creates a CSC representation of the given matrix.
	public static CSCMatrix of(MatrixReader m) {
		if (m == null)
			throw new NullPointerException("the given matrix is null");

		return switch (m) {
			case CSCMatrix csc -> csc.copy();
			case SparseMatrixReader sparse -> sparse.pack();
			default -> {
				var hpm = new HashPointMatrix(m.rows(), m.columns());
				m.iterate(hpm::set);
				yield hpm.pack();
			}
		};
	}

	@Override
	public CSCMatrix pack() {
		return this;
	}

	@Override
	public HashPointMatrix unpack() {
		var m = new HashPointMatrix(rows, columns);
		iterate(m::set);
		return m;
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public CSCMatrix copy() {
		double[] vals = Arrays.copyOf(values, values.length);
		int[] colPtr = Arrays.copyOf(columnPointers, columnPointers.length);
		int[] rowIdx = Arrays.copyOf(rowIndices, rowIndices.length);
		return new CSCMatrix(rows, columns, vals, colPtr, rowIdx);
	}

	@Override
	public double get(int row, int col) {
		int start = columnPointers[col];
		int end = columnPointers[col + 1];
		for (int idx = start; idx < end; idx++) {
			int r = rowIndices[idx];
			if (r == row)
				return values[idx];
		}
		return 0;
	}

	@Override
	public double[] getColumn(int i) {
		double[] v = new double[rows];
		int start = columnPointers[i];
		int end = columnPointers[i + 1];
		for (int idx = start; idx < end; idx++) {
			int r = rowIndices[idx];
			v[r] = values[idx];
		}
		return v;
	}

	@Override
	public double[] getRow(int i) {
		double[] v = new double[columns];
		for (int col = 0; col < columns; col++) {
			v[col] = get(i, col);
		}
		return v;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public void iterate(EntryFunction fn) {
		for (int col = 0; col < columns; col++) {
			int start = columnPointers[col];
			int end = columnPointers[col + 1];
			for (int i = start; i < end; i++) {
				int row = rowIndices[i];
				double val = values[i];
				if (val != 0) {
					fn.value(row, col, val);
				}
			}
		}
	}

	/// This method scales every column `j` of this matrix by `v[j]` _in place_.
	/// This is a fast operation for CSC matrices. Depending on the given vector,
	/// the `values` array may contain zero entries then. The `rowIndices` and the
	/// `columnPointers` are _not_ modified in this case.
	public void scaleColumns(double[] v) {
		for (int col = 0; col < columns; col++) {
			double factor = v[col];
			int start = columnPointers[col];
			int end = columnPointers[col + 1];
			for (int i = start; i < end; i++) {
				values[i] *= factor;
			}
		}
	}
}
