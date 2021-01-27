package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.Matrix;

/**
 * Stores the factorization of a square matrix `A` (e.g. a LU decomposition)
 * which can be efficiently used to calculate the `x` of an equation
 * `A * b = x` for a given `b`.
 */
public interface Factorization {

	/**
	 * Returns the number of rows which must be equal to the number of columns
	 * of the underlying matrix `A`.
	 */
	int size();

	/**
	 * By default creates a vector `b` with `b[i] = val` and calls `solve(b)`
	 * on this factorization. Implementers should override this method when
	 * they have a more efficient method available for this.
	 */
	default double[] solve(int i, double val) {
		double[] b = new double[size()];
		b[i] = val;
		return solve(b);
	}

	/**
	 * Calculates the `x` in `A * x = b` for the given `b`.
	 */
	double[] solve(double[] b);

	/**
	 * Calculates the `X` in `A * X = B` for the given matrix `B`. By default
	 * this calculates `X[:,j]` for each column `j` of `B[:,j]` separately.
	 * Implementers should overwrite this method if they have a more efficient
	 * method available.
	 */
	default Matrix solve(Matrix b) {
		var x = new DenseMatrix(size(), b.columns());
		for (int j = 0; j < b.columns(); j++) {
			var bj = b.getColumn(j);
			var xj = solve(bj);
			for (int i = 0; i < xj.length; i++) {
				x.set(i, j, xj[i]);
			}
		}
		return x;
	}

	/**
	 * Disposes the factorization (e.g. clears native memory etc.).
	 */
	void dispose();

	/**
	 * Returns true if this factorization is disposed.
	 */
	boolean isDisposed();

}
