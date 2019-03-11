package org.openlca.core.matrix.format;

import java.util.Arrays;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Implements a compressed-column representation of a sparse matrix. Note that
 * this format is not editable. Calling `set(row, col, val)` will throw an
 * exception.
 */
public class CCRMatrix implements IMatrix {

	/** The total number of rows. */
	public final int rows;

	/** The total number of columns. */
	public final int columns;

	/** The vector with non-zero entries $A.val$. */
	public final double[] values;

	/**
	 * The column pointers $A.c$ that indicate where each column begins. The
	 * last component of $A.c$ contains $\text{nnz}(A)$ where
	 * $\text{nnz}(A)$ is the number of non-zero entries in A.
	 */
	public final int[] columnPointers;

	/** The row indices $A.r$ of the non-zero entries $A.val$. */
	public final int[] rowIndices;

	private CCRMatrix(IMatrix other) {
		if (other instanceof CCRMatrix) {

			// copy a CCR matrix
			CCRMatrix ccr = (CCRMatrix) other;
			this.rows = ccr.rows;
			this.columns = ccr.columns;
			this.values = Arrays.copyOf(ccr.values, ccr.values.length);
			this.columnPointers = Arrays.copyOf(
					ccr.columnPointers, ccr.columnPointers.length);
			this.rowIndices = Arrays.copyOf(
					ccr.rowIndices, ccr.rowIndices.length);
		} else {

			// compress another matrix format
			this.rows = other.rows();
			this.columns = other.columns();
			this.columnPointers = new int[other.columns() + 1];
			TDoubleArrayList values = new TDoubleArrayList(other.rows());
			TIntArrayList rowIndices = new TIntArrayList(other.rows());
			int i = 0;
			for (int col = 0; col < this.columns; col++) {
				boolean foundEntry = false;
				for (int row = 0; row < this.rows; row++) {
					double val = other.get(row, col);
					if (val == 0)
						continue;
					values.add(val);
					rowIndices.add(row);
					if (!foundEntry) {
						this.columnPointers[col] = i;
						foundEntry = true;
					}
					i++;
				}
				if (!foundEntry) {
					this.columnPointers[col] = i;
				}
			}
			this.values = values.toArray();
			this.rowIndices = rowIndices.toArray();
			this.columnPointers[this.columns] = this.values.length;
		}
	}

	public static CCRMatrix of(IMatrix other) {
		if (other == null) {
			return new CCRMatrix(new HashPointMatrix());
		}
		return new CCRMatrix(other);
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public IMatrix copy() {
		return new CCRMatrix(this);
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
	public void set(int row, int col, double val) {
		throw new RuntimeException(
				"Modifying a compressed matrix is not supported");
	}

	@Override
	public void setValues(double[][] values) {
		throw new RuntimeException(
				"Modifying a compressed matrix is not supported");
	}
}
