package org.openlca.eigen;

import org.openlca.core.matrix.format.CompressedRowMatrix;

public class Eigen {

	/**
	 * Solves the system of linear equations A * B = X using SuperLU.
	 * 
	 * IMPORTANT: this function does not work if A is a matrix with a single
	 * entry (see the test in the test.cpp).
	 * 
	 * @param dim
	 *            the dimension of the sparse matrix A (A must be a square
	 *            matrix).
	 * @param n
	 *            the number of non-zero entries in the sparse matrix A
	 * @param rowsA
	 *            the row indices with values of matrix A (size = n).
	 * @param columnsA
	 *            the column indices with values of matrix A (size = n).
	 * @param dataA
	 *            the data values of the non-zero entries in A (size = n).
	 * @param b
	 *            the vector b (size = dim)
	 * @param x
	 *            the resulting vector x (size = dim).
	 */
	public static native void sparseLu(int dim, int n, int[] rowsA,
			int[] columnsA, double[] dataA, double[] b, double[] x);

	public static native void bicgstab(int dim, int n, int[] rowsA,
			int[] columnsA, double[] dataA, double[] b, double[] x);

	public static native void bicgstabInvert(CompressedRowMatrix matrix,
			double[] inverse);

	public static native void sparseLuInvert2(CompressedRowMatrix matrix,
			double[] inverse);

	public static native void sparseLuInvert(int dim, int n, int[] rowsA,
			int[] columnsA, double[] dataA, double[] inverse);

	public static native void sparseMmult(int rowsA, int k, int colsB,
			int entriesA, int entriesB, int[] rowIdxA, int[] colIdxA,
			double[] valsA, int[] rowIdxB, int[] colIdxB, double[] valsB,
			double[] result);

}
