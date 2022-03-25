package org.openlca.core.matrix.solvers;

import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.julia.Julia;
import org.openlca.nativelib.Module;
import org.openlca.nativelib.NativeLib;

public class NativeSolver implements MatrixSolver {

	public NativeSolver() {
	}

	@Override
	public boolean hasSparseSupport() {
		return NativeLib.isLoaded(Module.UMFPACK);
	}

	@Override
	public Matrix matrix(int rows, int columns) {
		return new DenseMatrix(rows, columns);
	}

	@Override
	public double[] solve(MatrixReader a, int idx, double d) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());

		if (hasSparseSupport() &&
				(a instanceof HashPointMatrix || a instanceof CSCMatrix)) {
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

		int info = Julia.solve(A.columns(), 1, lu.data, b);
		if (info > 0)
			throw new SingularMatrixException();
		if (info < 0)
			throw new InsufficientDataException();
		return b;
	}

	@Override
	public double[] multiply(MatrixReader m, double[] x) {
		if (m instanceof HashPointMatrix
				|| m instanceof CSCMatrix) {
			return m.multiply(x);
		}
		var a = MatrixConverter.dense(m);
		double[] y = new double[m.rows()];
		Julia.mvmult(m.rows(), m.columns(), a.data, x, y);
		return y;
	}

	@Override
	public DenseMatrix invert(MatrixReader a) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix i = _a == a ? _a.copy() : _a;
		int info = Julia.invert(_a.columns(), i.data);
		if (info > 0)
			throw new SingularMatrixException();
		if (info < 0)
			throw new InsufficientDataException();
		return i;
	}

	@Override
	public DenseMatrix multiply(MatrixReader a, MatrixReader b) {
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

	@Override
	public Factorization factorize(MatrixReader matrix) {
		if (!matrix.isSquare())
			throw new NonSquareMatrixException(matrix.rows(), matrix.columns());
		if (matrix instanceof HashPointMatrix) {
			var csc = ((HashPointMatrix) matrix).compress();
			return SparseFactorization.of(csc);
		}
		if (matrix instanceof CSCMatrix) {
			return SparseFactorization.of((CSCMatrix) matrix);
		}
		return DenseFactorization.of(matrix);
	}
}
