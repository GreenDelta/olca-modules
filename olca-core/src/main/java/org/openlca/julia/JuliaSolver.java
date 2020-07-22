package org.openlca.julia;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
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
		if (density < 0.4)
			return new HashPointMatrix(rows, cols);
		else
			return new DenseMatrix(rows, cols);
	}

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		if (a instanceof HashPointMatrix && Julia.isWithUmfpack()) {
			var csc = CSCMatrix.of(a);
			double[] f = new double[csc.rows];
			f[idx] = d;
			double[] b = new double[csc.rows];
			Julia.umfSolve(
				csc.rows,
				csc.columnPointers,
				csc.rowIndices,
				csc.values,
				f,
				b);
			return b;
		}
		var A = MatrixConverter.dense(a);
		var lu = A == a ? A.copy() : A;
		double[] b = new double[A.rows()];
		b[idx] = d;
		Julia.solve(A.columns(), 1, lu.data, b);
		return b;
	}

	@Override
	public double[] multiply(IMatrix m, double[] x) {
		if (m instanceof HashPointMatrix) {
			HashPointMatrix s = (HashPointMatrix) m;
			return s.multiply(x);
		}
		DenseMatrix a = MatrixConverter.dense(m);
		double[] y = new double[m.rows()];
		Julia.mvmult(m.rows(), m.columns(), a.data, x, y);
		return y;
	}

	@Override
	public DenseMatrix invert(IMatrix a) {
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix i = _a == a ? _a.copy() : _a;
		Julia.invert(_a.columns(), i.data);
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
		if (colsB == 1) {
			Julia.mvmult(rowsA, k, _a.data, _b.data, c.data);
		} else {
			Julia.mmult(rowsA, colsB, k, _a.data, _b.data, c.data);
		}
		return c;
	}
}
