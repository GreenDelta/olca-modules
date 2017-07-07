package org.openlca.eigen;

public class Lapack {

	/**
	 * Inverts the given matrix.
	 * 
	 * @param n
	 *            [in] the dimension of the matrix (n = rows = columns)
	 * @param a
	 *            [io] on entry: the matrix to be inverted, on exit: the inverse
	 *            (size = n * n)
	 * @return the LAPACK return code
	 */
	public static native int dInvert(int n, double[] a);

	/**
	 * Inverts the given matrix. (single precision)
	 * 
	 * @param n
	 *            [in] the dimension of the matrix (n = rows = columns)
	 * @param a
	 *            [io] on entry: the matrix to be inverted, on exit: the inverse
	 *            (size = n * n)
	 * @return the LAPACK return code
	 */
	public static native int sInvert(int n, float[] a);

	/**
	 * Solves a system of linear equations A * X = B for general matrices. It
	 * calls the LAPACK DGESV routine.
	 * 
	 * @param n
	 *            [in] the dimension of the matrix A (n = rows = columns of A)
	 * @param bColums
	 *            [in] the number of columns of the matrix B
	 * @param a
	 *            [io] on entry the matrix A, on exit the LU factorization of A
	 *            (size = n * n)
	 * @param b
	 *            [io] on entry the matrix B, on exit the solution of the
	 *            equation (size = n * bColums)
	 * @return the LAPACK return code
	 */
	public static native int dSolve(int n, int nrhs, double[] a, double[] b);

	/**
	 * Solves a system of linear equations A * X = B for general matrices. It
	 * calls the LAPACK DGESV routine. (single precision)
	 * 
	 * @param n
	 *            [in] the dimension of the matrix A (n = rows = columns of A)
	 * @param bColums
	 *            [in] the number of columns of the matrix B
	 * @param a
	 *            [io] on entry the matrix A, on exit the LU factorization of A
	 *            (size = n * n)
	 * @param b
	 *            [io] on entry the matrix B, on exit the solution of the
	 *            equation (size = n * bColums)
	 * @return the LAPACK return code
	 */
	public static native int sSolve(int rows, int bColums, float[] a, float[] b);

	/**
	 * Solves the system of linear equations A * X = B for general matrices in
	 * single precision with iterative refinement.
	 * 
	 * @TODO: add parameter doc
	 */
	public static native int dsSolve(int n, int nrhs, double[] a, double[] b,
			double[] x);

	/**
	 * Computes the LU factorization of a square matrix A.
	 * 
	 * @param n
	 *            [in] the dimension of a (n = rows = columns)
	 * @param a
	 *            [io] on entry the matrix to be factorized, on exit the LU
	 *            factorization of A (size = n*n).
	 * @param pivots
	 *            [out] the pivot indices of the factorization (size = n)
	 * @return the LAPACK return code (0=success)
	 */
	public static native int dLu(int n, double[] a, int[] pivots);

	/**
	 * Computes the LU factorization of a square matrix A. (single precision)
	 * 
	 * @param n
	 *            [in] the dimension of a (n = rows = columns)
	 * @param a
	 *            [io] on entry the matrix to be factorized, on exit the LU
	 *            factorization of A (size = n*n).
	 * @param pivots
	 *            [out] the pivot indices of the factorization (size = n)
	 * @return the LAPACK return code (0=success)
	 */
	public static native int sLu(int n, float[] a, int[] pivots);

	/**
	 * Solves A * X = B where A is already factorized.
	 * 
	 * @param rows
	 *            [in] the number of rows in matrix A
	 * @param bCols
	 *            [in] the number of columns of the right side
	 * @param lu
	 *            [in] the LU factorization of matrix A (size = rows * rows)
	 * @param pivots
	 *            [in] the pivot indices (see method lu)
	 * @param b
	 *            [io] on entry the right hand side of the equation, on exit the
	 *            solution of the equation (size = rows * bCols)
	 * @return the LAPACK return code (0=success)
	 */
	public static native int dSolveLu(int rows, int bCols, double[] lu,
			int[] pivots, double[] b);

	/**
	 * Solves A * X = B where A is already factorized. (single precision)
	 * 
	 * @param rows
	 *            [in] the number of rows in matrix A
	 * @param bCols
	 *            [in] the number of columns of the right side
	 * @param lu
	 *            [in] the LU factorization of matrix A (size = rows * rows)
	 * @param pivots
	 *            [in] the pivot indices (see method lu)
	 * @param b
	 *            [io] on entry the right hand side of the equation, on exit the
	 *            solution of the equation (size = rows * bCols)
	 * @return the LAPACK return code (0=success)
	 */
	public static native int sSolveLu(int rows, int bCols, float[] lu,
			int[] pivots, float[] b);

	public static native int dEquilibrate(int rows, int cols, double[] a,
			double[] r, double[] c);

	public static native int sEquilibrate(int rows, int cols, float[] a,
			float[] r, float[] c);

}
