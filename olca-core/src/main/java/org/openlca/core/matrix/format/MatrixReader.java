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
	 * Returns true when this matrix has the same number of rows and columns.
	 */
	default boolean isSquare() {
		int rows = rows();
		int cols = columns();
		return rows == cols && rows > 0;
	}

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

	/**
	 * Copies the content of this matrix to the given matrix. The given matrix can
	 * be smaller or larger than this matrix.
	 */
	default void copyTo(Matrix matrix) {
		int cols = Math.min(this.columns(), matrix.columns());
		int rows = Math.min(this.rows(), matrix.rows());
		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				matrix.set(row, col, this.get(row, col));
			}
		}
	}

	/**
	 * Copies the content of this matrix to the given matrix. The given matrix can
	 * be smaller or larger than this matrix.
	 *
	 * @param matrix    the target matrix
	 * @param rowOffset the row offset in the target matrix
	 * @param colOffset the column offset in the target matrix
	 */
	default void copyTo(Matrix matrix, int rowOffset, int colOffset) {

		int cols = Math.min(matrix.columns() - colOffset, this.columns());
		int rows = Math.min(matrix.rows() - rowOffset, this.rows());

		if (matrix instanceof DenseMatrix denseTarget) {

			// using system-array-copy when both matrices are dense
			if (this instanceof DenseMatrix denseSource) {
				for (int col = 0; col < cols; col++) {
					System.arraycopy(
						denseSource.data, col * denseSource.rows,
						denseTarget.data, ((col + colOffset) * denseTarget.rows) + rowOffset,
						rows);
				}
				return;
			}

			// use a single buffer when the target is dense
			var buffer = new double[rows];
			for (int col = 0; col < cols; col++) {
				this.readColumn(col, buffer);
				System.arraycopy(
					buffer, 0,
					denseTarget.data, ((col + colOffset) * denseTarget.rows) + rowOffset,
					rows);
			}

			return;
		}

		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				matrix.set(row + rowOffset, col + colOffset, this.get(row, col));
			}
		}
	}

	/**
	 * Reads the data from the given row into the given buffer without additional
	 * memory allocations.
	 */
	default void readRow(int row, double[] buffer) {
		int cols = Math.min(columns(), buffer.length);
		for (int col = 0; col < cols; col++) {
			buffer[col] = get(row, col);
		}
	}

	/**
	 * Reads the data from the given column into the given buffer without
	 * additional memory allocations.
	 */
	default void readColumn(int column, double[] buffer) {
		int rows = Math.min(rows(), buffer.length);
		for (int row = 0; row < rows; row++) {
			buffer[row] = get(row, column);
		}
	}
}
