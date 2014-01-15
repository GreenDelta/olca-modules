package org.openlca.core.math;

/**
 * Interface for linear algebra and matrix problems that we need to solve in
 * openLCA. We provide different implementations for these functions also in
 * other packages which are based on high performance libraries like Eigen and
 * OpenBLAS.
 */
public interface IMatrixSolver {

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
	IMatrix multiply(IMatrix a, IMatrix b);

	/**
	 * Calculates a matrix-vector product. In openLCA we use this for example
	 * when we calculate the inventory result: g = B * s
	 */
	double[] multiply(IMatrix m, double[] v);

	/**
	 * Returns the matrix factory of this solver.
	 */
	IMatrixFactory<?> getMatrixFactory();

}
