package org.openlca.core.matrix.format;

import org.openlca.core.model.Copyable;

public interface MatrixReader extends Copyable<MatrixReader> {

	/**
	 * Returns the number of rows of the matrix.
	 */
	int rows();

	/**
	 * Returns the number of columns of the matrix.
	 */
	int columns();

	/**
	 * Get the value of the given row and column.
	 */
	double get(int row, int col);

	/**
	 * Get the row values of the given column.
	 */
	double[] getColumn(int i);

	/**
	 * Get the column values of the given row.
	 */
	double[] getRow(int i);

	/**
	 * Iterates over the non-zero values in this matrix. There is no defined
	 * order in which the matrix entries are processed. Specifically sparse
	 * matrix layouts should overwrite this function with faster implementations.
	 */
	default void iterate(EntryFunction fn) {
		if (fn == null)
			return;
		for (int col = 0; col < columns(); col++) {
			for (int row = 0; row < rows(); row++) {
				double val = get(row, col);
				if (val != 0) {
					fn.value(row, col, val);
				}
			}
		}
	}

	/**
	 * Performs a matrix-vector multiplication with the given vector v. It uses
	 * the iterate function which can be fast for sparse matrices. For dense
	 * matrices it can be much faster to call into native code instead of using
	 * this method.
	 */
	default double[] multiply(double[] v) {
		double[] x = new double[rows()];
		iterate((row, col, val) -> x[row] += val * v[col]);
		return x;
	}

	/**
	 * Returns the diagonal of this matrix.
	 */
	default double[] diag() {
		var rows = rows();
		var cols = columns();
		var n = Math.min(rows, cols);
		var diag = new double[n];
		for (int i = 0; i < n; i++) {
			diag[i] = get(i, i);
		}
		return diag;
	}

	/**
	 * Converts this (immutable) reader into a mutable matrix. Returns itself
	 * if this reader is already a mutable matrix. Otherwise it creates a
	 * mutable copy.
	 */
	default Matrix asMutable() {
		if (this instanceof Matrix)
			return (Matrix) this;
		var m = isSparse()
			? new HashPointMatrix(rows(), columns())
			: new DenseMatrix(rows(), columns());
		iterate(m::set);
		return m;
	}

	/**
	 * Same as `asMutable` but with the guarantee that a copy is returned.
	 */
	default Matrix asMutableCopy() {
		if (this instanceof Matrix)
			return ((Matrix) this).copy();
		var m = isSparse()
			? new HashPointMatrix(rows(), columns())
			: new DenseMatrix(rows(), columns());
		iterate(m::set);
		return m;
	}

	/**
	 * Returns true if the type of this implementation is a sparse matrix
	 * structure. It does **not** look into this matrix and checks if it is
	 * filled sparsely but just returns true if the storage type is optimized
	 * for sparsity.
	 */
	boolean isSparse();

	/**
	 * A matrix is dense if it is not sparse (see `isSparse`)
	 */
	default boolean isDense() {
		return !isSparse();
	}
}
