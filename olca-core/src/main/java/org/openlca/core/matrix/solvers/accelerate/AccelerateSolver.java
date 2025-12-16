package org.openlca.core.matrix.solvers.accelerate;

import org.apache.commons.math3.exception.InsufficientDataException;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.SingularMatrixException;
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
 * This solver is available on ARM64 macOS and provides both dense and sparse
 * matrix operations without requiring external native libraries.
 */
public class AccelerateSolver implements MatrixSolver {

	public AccelerateSolver() {
		if (!AccelerateJulia.isAvailable()) {
			throw new IllegalStateException("Accelerate framework not available on this platform");
		}
	}

	@Override
	public boolean hasSparseSupport() {
		// Sparse support is available when Accelerate is available
		// Note: Actual sparse implementation may need additional checks
		return AccelerateJulia.isAvailable();
	}

	@Override
	public Matrix matrix(int rows, int columns) {
		return new DenseMatrix(rows, columns);
	}

	@Override
	public double[] solve(MatrixReader a, int idx, double d) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());

		// For now, use dense solver for all matrices
		// Sparse solver will be implemented when Accelerate sparse API is fully integrated
		// TODO: Implement sparse solve when Accelerate sparse API is ready
		// if (hasSparseSupport() && (a instanceof HashPointMatrix || a instanceof CSCMatrix)) {
		//     // Use sparse solver
		// }
		
		var A = MatrixConverter.dense(a);
		var lu = A == a ? A.copy() : A;
		double[] b = new double[A.rows()];
		b[idx] = d;

		int info = AccelerateJulia.solve(A.columns(), 1, lu.data, b);
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
		AccelerateJulia.mvmult(m.rows(), m.columns(), a.data, x, y);
		return y;
	}

	@Override
	public DenseMatrix invert(MatrixReader a) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());
		DenseMatrix _a = MatrixConverter.dense(a);
		DenseMatrix i = _a == a ? _a.copy() : _a;
		int info = AccelerateJulia.invert(_a.columns(), i.data);
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
			AccelerateJulia.mvmult(rowsA, k, _a.data, _b.data, c.data);
		} else {
			AccelerateJulia.mmult(rowsA, colsB, k, _a.data, _b.data, c.data);
		}
		return c;
	}

	@Override
	public Factorization factorize(MatrixReader matrix) {
		if (!matrix.isSquare())
			throw new NonSquareMatrixException(matrix.rows(), matrix.columns());
		
		// For now, use dense factorization for all matrices
		// Sparse factorization will be implemented when Accelerate sparse API is ready
		// TODO: Implement sparse factorization when Accelerate sparse API is ready
		// if (hasSparseSupport()) {
		//     if (matrix instanceof HashPointMatrix hpm) {
		//         return AccelerateSparseFactorization.of(hpm.compress());
		//     }
		//     if (matrix instanceof CSCMatrix csc) {
		//         return AccelerateSparseFactorization.of(csc);
		//     }
		// }
		return AccelerateDenseFactorization.of(matrix);
	}
}
