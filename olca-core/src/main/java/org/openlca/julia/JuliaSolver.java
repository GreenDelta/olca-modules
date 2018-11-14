package org.openlca.julia;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.core.matrix.solvers.IMatrixSolver;

public class JuliaSolver implements IMatrixSolver {

	@Override
	public IMatrix matrix(int rows, int columns) {
		return new DenseMatrix(rows, columns);
	}

	@Override
	public IMatrix matrix(int rows, int cols, double density) {
		if (density < 0.25)
			return new HashMatrix(rows, cols);
		else
			return new DenseMatrix(rows, cols);
	}

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		DenseMatrix A = MatrixConverter.dense(a);
		DenseMatrix lu = A == a ? A.copy() : A;
		double[] b = new double[A.rows()];
		b[idx] = d;
		Julia.solve(A.columns(), 1, lu.getData(), b);
		return b;
	}

	@Override
	public double[] multiply(IMatrix m, double[] x) {
		if (m instanceof HashMatrix) {
			HashMatrix s = (HashMatrix) m;
			return s.multiply(x);
		}
		DenseMatrix a = MatrixConverter.dense(m);
		double[] y = new double[m.rows()];
		Julia.mvmult(m.rows(), m.columns(), a.getData(), x, y);
		return y;
	}

	@Override
	public DenseMatrix invert(IMatrix a) {
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix i = _a == a ? _a.copy() : _a;
		Julia.invert(_a.columns(), i.getData());
		return i;
	}

	@Override
	public DenseMatrix multiply(IMatrix a, IMatrix b) {
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix _b = MatrixConverter.dense(b);
		int rowsA = _a.rows();
		int colsB = _b.columns();
		int k = _a.columns();
		DenseMatrix c = new DenseMatrix(rowsA, colsB);
		if (colsB == 1)
			Julia.mvmult(rowsA, k, _a.getData(), _b.getData(), c.getData());
		else
			Julia.mmult(rowsA, colsB, k, _a.getData(), _b.getData(),
					c.getData());
		return c;
	}

	@Override
	public void scaleColumns(IMatrix m, double[] v) {
		if (m instanceof HashMatrix) {
			HashMatrix s = (HashMatrix) m;
			s.scaleColumns(v);
			return;
		}
		for (int row = 0; row < m.rows(); row++) {
			for (int col = 0; col < m.columns(); col++) {
				m.set(row, col, v[col] * m.get(row, col));
			}
		}
	}

}
