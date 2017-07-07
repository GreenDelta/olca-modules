package org.openlca.eigen;

public class Blas {

	/**
	 * Matrix-matrix multiplication: C := A * B
	 * 
	 * @param rowsA
	 *            [in] number of rows of matrix A
	 * @param colsB
	 *            [in] number of columns of matrix B
	 * @param k
	 *            [in] number of columns of matrix A and number of rows of
	 *            matrix B
	 * @param a
	 *            [in] matrix A (size = rowsA*k)
	 * @param b
	 *            [in] matrix B (size = k * colsB)
	 * @param c
	 *            [out] matrix C (size = rowsA * colsB)
	 */
	public static native void dMmult(int rowsA, int colsB, int k,
			double[] a, double[] b, double[] c);

	/**
	 * Matrix-matrix multiplication: C := A * B (single precision)
	 * 
	 * @param rowsA
	 *            [in] number of rows of matrix A
	 * @param colsB
	 *            [in] number of columns of matrix B
	 * @param k
	 *            [in] number of columns of matrix A and number of rows of
	 *            matrix B
	 * @param a
	 *            [in] matrix A (size = rowsA*k)
	 * @param b
	 *            [in] matrix B (size = k * colsB)
	 * @param c
	 *            [out] matrix C (size = rowsA * colsB)
	 */
	public static native void sMmult(int rowsA, int colsB,
			int k, float[] a, float[] b, float[] c);

	/**
	 * Matrix-vector multiplication: y:= A * x
	 * 
	 * @param rowsA
	 *            [in] rows of matrix A
	 * @param colsA
	 *            [in] columns of matrix A
	 * @param a
	 *            [in] the matrix A
	 * @param x
	 *            [in] the vector x
	 * @param y
	 *            [out] the resulting vector y
	 */
	public static native void dMVmult(int rowsA, int colsA,
			double[] a, double[] x, double[] y);

	/**
	 * Matrix-vector multiplication: y:= A * x (single precision)
	 * 
	 * @param rowsA
	 *            [in] rows of matrix A
	 * @param colsA
	 *            [in] columns of matrix A
	 * @param a
	 *            [in] the matrix A
	 * @param x
	 *            [in] the vector x
	 * @param y
	 *            [out] the resulting vector y
	 */
	public static native void sMVmult(int rowsA, int colsA,
			float[] a, float[] x, float[] y);

}
