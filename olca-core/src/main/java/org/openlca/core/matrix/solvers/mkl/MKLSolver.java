package org.openlca.core.matrix.solvers.mkl;

import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.Factorization;
import org.openlca.core.matrix.solvers.MatrixSolver;

import java.util.Optional;

public class MKLSolver implements MatrixSolver {

	@Override
	public boolean hasSparseSupport() {
		return true;
	}

	@Override
	public Matrix matrix(int rows, int columns) {
		return new HashPointMatrix(rows, columns);
	}

	@Override
	public double[] solve(MatrixReader a, int idx, double d) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());

		int n = a.rows();
		var b = new double[n];
		b[idx] = d;

		var csc = asSparse(a).orElse(null);
		if (csc != null) {
			var x = new double[n];
			int info = MKL.solveSparse(
				n,
				csc.values,
				csc.rowIndices,
				csc.columnPointers,
				b,
				x
			);
			InfoCode.checkPardiso(info);
			return x;
		}

		var dense = MatrixConverter.dense(a);
		var lu = dense == a ? dense.copy() : dense;
		int info = MKL.solveDense(n, 1, lu.data, b);
		InfoCode.checkBlas(info);
		return b;
	}

	@Override
	public double[] multiply(MatrixReader m, double[] x) {
		// TODO: check/add native support
		if (m instanceof HashPointMatrix || m instanceof CSCMatrix)
			return m.multiply(x);
		var a = MatrixConverter.dense(m);
		var y = new double[m.rows()];
		MKL.denseMatrixVectorMul(m.rows(), m.columns(), a.data, x, y);
		return y;
	}

	@Override
	public Matrix invert(MatrixReader a) {
		if (!a.isSquare())
			throw new NonSquareMatrixException(a.rows(), a.columns());
		// TODO: add sparse support
		var dense = MatrixConverter.dense(a);
		var inv = dense == a ? dense.copy() : dense;
		int info = MKL.invertDense(dense.columns(), inv.data);
		InfoCode.checkBlas(info);
		return inv;
	}

	@Override
	public Matrix multiply(MatrixReader a, MatrixReader b) {
		// TODO: add sparse support
		var denseA = MatrixConverter.dense(a);
		var denseB = MatrixConverter.dense(b);
		int m = denseA.rows();
		int n = denseB.columns();
		int k = denseA.columns();
		var c = new DenseMatrix(m, n);
		if (n == 1) {
			MKL.denseMatrixVectorMul(
				m, k, denseA.data, denseB.data, c.data);
		} else {
			MKL.denseMatrixMul(
				m, n, k, denseA.data, denseB.data, c.data);
		}
		return c;
	}

	@Override
	public Factorization factorize(MatrixReader matrix) {
		var csc = asSparse(matrix).orElse(null);
		var ptr = new long[1];
		if (csc != null) {
			int info = MKL.sparseFactorization(
				csc.rows,
				csc.values,
				csc.rowIndices,
				csc.columnPointers,
				ptr
			);
			InfoCode.checkPardiso(info);
			return new SparseFactorization(ptr[0], csc.rows);
		}

		var dense = DenseMatrix.of(matrix);
		int info = MKL.denseFactorization(dense.rows, dense.data, ptr);
		InfoCode.checkBlas(info);
		return new DenseFactorization(ptr[0], dense.rows);
	}

	@Override
	public boolean isNative() {
		return true;
	}

	private Optional<CSCMatrix> asSparse(MatrixReader matrix) {
		if (matrix instanceof CSCMatrix csc)
			return Optional.of(csc);
		if (matrix instanceof HashPointMatrix hpm)
			return Optional.of(hpm.compress());
		return Optional.empty();
	}
}
