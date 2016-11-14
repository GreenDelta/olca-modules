package org.openlca.eigen.solvers;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.eigen.Blas;
import org.openlca.eigen.DenseMatrix;
import org.openlca.eigen.DenseMatrixFactory;
import org.openlca.eigen.Lapack;
import org.openlca.eigen.MatrixConverter;

/**
 * A double precision solver that uses dense matrices and calls the respective
 */
public class DenseSolver implements IMatrixSolver {

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		DenseMatrix A = MatrixConverter.asDenseMatrix(a);
		DenseMatrix lu = A.copy();
		double[] b = new double[a.rows()];
		b[idx] = d;
		Lapack.dSolve(A.columns(), 1, lu.getData(), b);
		return b;
	}

	@Override
	public double[] multiply(IMatrix m, double[] x) {
		DenseMatrix a = MatrixConverter.asDenseMatrix(m);
		double[] y = new double[m.rows()];
		Blas.dMVmult(m.rows(), m.columns(), a.getData(),
				x, y);
		return y;
	}

	@Override
	public DenseMatrix invert(IMatrix a) {
		DenseMatrix _a = MatrixConverter.asDenseMatrix(a);
		DenseMatrix i = _a.copy();
		Lapack.dInvert(_a.columns(), i.getData());
		return i;
	}

	@Override
	public DenseMatrix multiply(IMatrix a, IMatrix b) {
		DenseMatrix _a = MatrixConverter.asDenseMatrix(a);
		DenseMatrix _b = MatrixConverter.asDenseMatrix(b);
		int rowsA = _a.rows();
		int colsB = _b.columns();
		int k = _a.columns();
		DenseMatrix c = new DenseMatrix(rowsA, colsB);
		if (colsB == 1)
			Blas.dMVmult(rowsA, k, _a.getData(), _b.getData(), c.getData());
		else
			Blas.dMmult(rowsA, colsB, k, _a.getData(), _b.getData(),
					c.getData());
		return c;
	}

	@Override
	public void scaleColumns(IMatrix m, double[] v) {
		for (int row = 0; row < m.rows(); row++) {
			for (int col = 0; col < m.columns(); col++) {
				m.set(row, col, v[col] * m.get(row, col));
			}
		}
	}

	@Override
	public IMatrixFactory<?> getMatrixFactory() {
		return new DenseMatrixFactory();
	}

}
