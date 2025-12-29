package org.openlca.core.matrix.solvers.accelerate;

import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.openlca.core.matrix.format.AccelerateSparseMatrix;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.Factorization;
import org.openlca.core.matrix.solvers.MatrixSolver;

/**
 * Matrix solver implementation using Apple Accelerate framework via Java 21 FFI.
 */
public class AccelerateSolver implements MatrixSolver {

	public AccelerateSolver() {
		if (!AccelerateFFI.isAvailable()) {
			throw new IllegalStateException("Accelerate framework not available on this platform");
		}
	}

	@Override
	public boolean hasSparseSupport() {
		return AccelerateFFI.isSparseFactorizationAvailable();
	}

	@Override
	public Matrix matrix(int rows, int columns) {
		return new DenseMatrix(rows, columns);
	}

	@Override
	public double[] solve(MatrixReader a, int idx, double d) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());

		// Use dense solver for all matrices		
		var A = MatrixConverter.dense(a);
		var lu = A == a ? A.copy() : A;
		double[] b = new double[A.rows()];
		b[idx] = d;

		int info = AccelerateFFI.solve(A.columns(), 1, lu.data, b);
		if (info > 0)
			throw new SingularMatrixException();
		if (info < 0)
			throw new InsufficientDataException();
		return b;
	}

	@Override
	public double[] multiply(MatrixReader m, double[] x) {
		if (m instanceof HashPointMatrix || m instanceof CSCMatrix) {
			return m.multiply(x);
		}
		var a = MatrixConverter.dense(m);
		double[] y = new double[m.rows()];
		AccelerateFFI.mvmult(m.rows(), m.columns(), a.data, x, y);
		return y;
	}

	@Override
	public DenseMatrix invert(MatrixReader a) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix i = _a == a ? _a.copy() : _a;
		int info = AccelerateFFI.invert(_a.columns(), i.data);
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
			AccelerateFFI.mvmult(rowsA, k, _a.data, _b.data, c.data);
		} else {
			AccelerateFFI.mmult(rowsA, colsB, k, _a.data, _b.data, c.data);
		}
		return c;
	}

	@Override
	public Factorization factorize(MatrixReader matrix) {
		if (!matrix.isSquare())
			throw new NonSquareMatrixException(matrix.rows(), matrix.columns());
		
		if (hasSparseSupport()) {
			// Prefer AccelerateSparseMatrix for zero conversion overhead
			if (matrix instanceof AccelerateSparseMatrix asm) {
				return AccelerateSparseFactorization.of(asm);
			}
			if (matrix instanceof HashPointMatrix hpm) {
				return AccelerateSparseFactorization.of(
						AccelerateSparseMatrix.of(hpm));
			}
			if (matrix instanceof CSCMatrix csc) {
				return AccelerateSparseFactorization.of(
						AccelerateSparseMatrix.of(csc));
			}
		}
		return AccelerateDenseFactorization.of(matrix);
	}
}
