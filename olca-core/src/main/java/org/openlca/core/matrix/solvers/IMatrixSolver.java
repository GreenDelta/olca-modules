package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.IMatrix;

/**
 * Interface for linear algebra and matrix problems that we need to solve in
 * openLCA. We provide different implementations for these functions also in
 * other packages which are based on high performance libraries like Eigen and
 * OpenBLAS.
 */
public interface IMatrixSolver {

	/**
	 * Creates a new matrix that can be used with this solver.
	 */
	IMatrix matrix(int rows, int columns);

	/**
	 * Solves the system of linear equations A * s = d. In openLCA this is used
	 * to calculate the scaling factors of an inventory where the vector d has
	 * just a single entry.
	 * 
	 * @param a
	 *            the technology matrix A
	 * @param d
	 *            the demand value (the entry in the vector d).
	 * 
	 * @param idx
	 *            the index of the entry in the demand vector.
	 * 
	 * @return the calculated scaling vector s
	 */
	double[] solve(IMatrix a, int idx, double d);

	/**
	 * Calculates the inverse of the given matrix.
	 */
	IMatrix invert(IMatrix a);

	/**
	 * Returns the matrix product of the given matrices.
	 */
	default IMatrix multiply(IMatrix a, IMatrix b) {
		if (a == null || b == null)
			return null;
		if (a.columns() != b.rows())
			throw new IllegalArgumentException("a.columns != b.rows");
		IMatrix r = matrix(a.rows(), b.columns());
		for (int row = 0; row < a.rows(); row++) {
			for (int col = 0; col < b.columns(); col++) {
				double val = 0;
				for (int k = 0; k < a.columns(); k++) {
					val += a.get(row, k) * b.get(k, col);
				}
				r.set(row, col, val);
			}
		}
		return r;
	}

	/**
	 * Calculates a matrix-vector product. In openLCA we use this for example
	 * when we calculate the inventory result: g = B * s
	 */
	default double[] multiply(IMatrix m, double[] v) {
		if (m == null || v == null)
			return null;
		int cols = Math.min(m.columns(), v.length);
		int rows = m.rows();
		double[] r = new double[rows];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				r[row] += (m.get(row, col) * v[col]);
			}
		}
		return r;
	}

	/**
	 * Scales the columns of matrix m with the factors in v. This is equivalent
	 * to m * diag(v) but can be implemented in a more efficient way.
	 */
	default void scaleColumns(IMatrix m, double[] v) {
		for (int row = 0; row < m.rows(); row++) {
			for (int col = 0; col < m.columns(); col++) {
				m.set(row, col, v[col] * m.get(row, col));
			}
		}
	}

}
