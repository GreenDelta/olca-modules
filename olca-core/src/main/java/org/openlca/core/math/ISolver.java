package org.openlca.core.math;

/**
 * Interface for linear algebra problems that we need to solve in openLCA. We
 * provide different implementations for these functions also in other packages
 * which are based on high performance libraries like Eigen and OpenBLAS.
 */
public interface ISolver {

	/**
	 * Solves the system of linear equations A * X = B.
	 * 
	 * @param a
	 *            the matrix A
	 * @param b
	 *            the right side of the equation B
	 * @return the resulting vector X
	 */
	IMatrix solve(IMatrix a, IMatrix b);

	/**
	 * Returns the inverse of the given matrix.
	 */
	IMatrix invert(IMatrix a);

	/**
	 * Returns the matrix product of the given matrices.
	 */
	IMatrix multiply(IMatrix a, IMatrix b);

	/**
	 * Returns the matrix factory of this solver.
	 */
	IMatrixFactory<?> getMatrixFactory();

}
