package org.openlca.core.matrix.format;

import java.util.Arrays;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Implements a compressed-column representation of a sparse matrix (CSC =
 * compressed sparse column). Note that this format is not editable. Calling
 * `set(row, col, val)` will throw an exception.
 */
public class CSCMatrix implements MatrixReader {

	/**
	 * The total number of rows.
	 */
	public final int rows;

	/**
	 * The total number of columns.
	 */
	public final int columns;

	/**
	 * The vector with non-zero entries $A.val$.
	 */
	public final double[] values;

	/**
	 * The column pointers $A.c$ that indicate where each column begins. The
	 * last component of $A.c$ contains the number of non-zero entries $nz(A)$.
	 */
	public final int[] columnPointers;

	/**
	 * The row indices $A.r$ of the non-zero entries $A.val$.
	 */
	public final int[] rowIndices;

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

	/**
	 * Creates a compressed sparse column representation of the given matrix.
	 */
	public static CSCMatrix of(MatrixReader m) {
		if (m == null)
			throw new NullPointerException("the given matrix is null");

		if (m instanceof CSCMatrix) {
			// copy a CCR matrix
			var csc = (CSCMatrix) m;
			double[] values = Arrays.copyOf(
					csc.values, csc.values.length);
			int[] columnPointers = Arrays.copyOf(
					csc.columnPointers, csc.columnPointers.length);
			int[] rowIndices = Arrays.copyOf(
					csc.rowIndices, csc.rowIndices.length);
			return new CSCMatrix(csc.rows, csc.columns,
					values, columnPointers, rowIndices);
		}

		if (m instanceof HashPointMatrix) {
			return ((HashPointMatrix) m).compress();
		}

		// compress another matrix format
		int[] columnPointers = new int[m.columns() + 1];
		var values = new TDoubleArrayList(m.rows());
		var rowIndices = new TIntArrayList(m.rows());
		int i = 0;
		for (int col = 0; col < m.columns(); col++) {
			boolean foundEntry = false;
			for (int row = 0; row < m.rows(); row++) {
				double val = m.get(row, col);
				if (val == 0)
					continue;
				values.add(val);
				rowIndices.add(row);
				if (!foundEntry) {
					columnPointers[col] = i;
					foundEntry = true;
				}
				i++;
			}
			if (!foundEntry) {
				columnPointers[col] = i;
			}
		}
		columnPointers[m.columns()] = values.size();

		return new CSCMatrix(m.rows(), m.columns(),
				values.toArray(), columnPointers, rowIndices.toArray());
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public MatrixReader copy() {
		return CSCMatrix.of(this);
	}

	@Override
	public double get(int row, int col) {
		int idxStart = columnPointers[col];
		int idxEnd = col == (columns - 1)
				? rowIndices.length
				: columnPointers[col + 1];
		for (int idx = idxStart; idx < idxEnd; idx++) {
			int r = rowIndices[idx];
			if (r == row)
				return values[idx];
		}
		return 0;
	}

	@Override
	public double[] getColumn(int i) {
		double[] v = new double[rows];
		int idxStart = columnPointers[i];
		int idxEnd = i == (columns - 1)
				? rowIndices.length
				: columnPointers[i + 1];
		for (int idx = idxStart; idx < idxEnd; idx++) {
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
			int end = col < (columns - 1)
					? columnPointers[col + 1]
					: values.length;
			for (int i = start; i < end; i++) {
				int row = rowIndices[i];
				double val = values[i];
				if (val != 0) {
					fn.value(row, col, val);
				}
			}
		}
	}

	/**
	 * Note that this method changes the data of this matrix in place. This is
	 * a fast operation of CSC matrices.
	 */
	public void scaleColumns(double[] v) {
		for (int col = 0; col < columns; col++) {
			double factor = v[col];
			int start = columnPointers[col];
			int end = col < (columns - 1)
					? columnPointers[col + 1]
					: values.length;
			for (int i = start; i < end; i++) {
				values[i] *= factor;
			}
		}
	}

}
