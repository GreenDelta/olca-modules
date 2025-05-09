package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.mkl.MKL;
import org.openlca.core.matrix.solvers.mkl.MKLSolver;
import org.openlca.nativelib.NativeLib;

/**
 * Interface for linear algebra and matrix problems that we need to solve in
 * openLCA. We provide different implementations for these functions also in
 * other packages which are based on high performance libraries like Eigen and
 * OpenBLAS.
 */
public interface MatrixSolver {

	/**
	 * Returns the best available matrix solver. If a native libraries are loaded
	 * it will create a solver using these libraries, otherwise it will fall back
	 * to a pure Java implementation which can be very slow for large matrices.
	 * Thus, you should try to always load native libraries before calling this
	 * function.
	 */
	static MatrixSolver get() {
		if (MKL.isLoaded())
			return new MKLSolver();
		if (NativeLib.isLoaded())
			return new NativeSolver();
		return new JavaSolver();
	}

	/**
	 * Returns true if the solver has specific support for solving sparse
	 * matrices efficiently.
	 */
	boolean hasSparseSupport();

	/**
	 * Creates an instance of the default matrix type that can be used with this
	 * solver.
	 */
	Matrix matrix(int rows, int columns);

	/**
	 * Solves the system of linear equations A * s = d. In openLCA this is used
	 * to calculate the scaling factors of an inventory where the vector d has
	 * just a single entry.
	 *
	 * @param a   the technology matrix A
	 * @param d   the demand value (the entry in the vector d).
	 * @param idx the index of the entry in the demand vector.
	 * @return the calculated scaling vector s
	 */
	double[] solve(MatrixReader a, int idx, double d);

	/**
	 * Calculates the inverse of the given matrix.
	 */
	Matrix invert(MatrixReader a);

	/**
	 * Returns the matrix product of the given matrices.
	 */
	default Matrix multiply(MatrixReader a, MatrixReader b) {
		if (a == null || b == null)
			return null;
		if (a.columns() != b.rows())
			throw new IllegalArgumentException("a.columns != b.rows");
		Matrix r = matrix(a.rows(), b.columns());
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
	default double[] multiply(MatrixReader m, double[] v) {
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

	Factorization factorize(MatrixReader matrix);

	/**
	 * Returns true if this is a native solver. That is a solver linked to a
	 * platform native high-performance math-library.
	 */
	default boolean isNative() {
		return this instanceof NativeSolver || this instanceof MKLSolver;
	}

	default double dot(double[] a, double[] b) {
		if (a == null || b == null)
			return 0;
		int n = Math.min(a.length, b.length);
		double s = 0.0;
		for (int i = 0; i < n; i++) {
			s += a[i] * b[i];
		}
		return s;
	}
}
