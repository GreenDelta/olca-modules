package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.eigen.Blas;
import org.openlca.eigen.Lapack;

/**
 * A double precision solver that uses dense matrices and calls the respective
 *
 * @deprecated The JuliaSolver should be used now (and may gets a different name)
 * instead of this solver. This solver and its dependencies will be removed.
 */
@Deprecated
public class DenseSolver {

	public IMatrix matrix(int rows, int columns) {
		return new DenseMatrix(rows, columns);
	}

	public double[] solve(IMatrix a, int idx, double d) {
		DenseMatrix A = MatrixConverter.dense(a);
		DenseMatrix lu = A.copy();
		double[] b = new double[a.rows()];
		b[idx] = d;
		Lapack.dSolve(A.columns(), 1, lu.data, b);
		return b;
	}

	public double[] multiply(IMatrix m, double[] x) {
		DenseMatrix a = MatrixConverter.dense(m);
		double[] y = new double[m.rows()];
		Blas.dMVmult(m.rows(), m.columns(), a.data, x, y);
		return y;
	}

	public DenseMatrix invert(IMatrix a) {
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix i = _a.copy();
		Lapack.dInvert(_a.columns(), i.data);
		return i;
	}

	public DenseMatrix multiply(IMatrix a, IMatrix b) {
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix _b = MatrixConverter.dense(b);
		int rowsA = _a.rows();
		int colsB = _b.columns();
		int k = _a.columns();
		DenseMatrix c = new DenseMatrix(rowsA, colsB);
		if (colsB == 1) {
			Blas.dMVmult(rowsA, k, _a.data, _b.data, c.data);
		} else {
			Blas.dMmult(rowsA, colsB, k, _a.data, _b.data, c.data);
		}
		return c;
	}
}
