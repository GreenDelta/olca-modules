package org.openlca.core.matrix.solvers.accelerate;

import java.util.Arrays;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.core.matrix.solvers.Factorization;

/**
 * Sparse matrix factorization using Accelerate framework.
 */
public class AccelerateSparseFactorization implements Factorization {

	private final int n;
	private final long pointer;
	private boolean isDisposed;

	private AccelerateSparseFactorization(int n, long pointer) {
		this.n = n;
		this.pointer = pointer;
	}

	public static AccelerateSparseFactorization of(CSCMatrix matrix) {
		var pointer = AccelerateFFI.createSparseFactorization(
				matrix.rows,
				matrix.columnPointers,
				matrix.rowIndices,
				matrix.values);
		return new AccelerateSparseFactorization(matrix.rows, pointer);
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public double[] solve(int idx, double d) {
		double[] b = new double[n];
		b[idx] = d;
		double[] x = new double[n];
		AccelerateFFI.solveSparseFactorization(pointer, b, x);
		return x;
	}

	@Override
	public double[] solve(double[] b) {
		double[] x = new double[n];
		AccelerateFFI.solveSparseFactorization(pointer, b, x);
		return x;
	}

	@Override
	public Matrix solve(Matrix b) {
		// For sparse factorization, convert to dense and solve column by column
		var denseB = MatrixConverter.dense(b);
		double[] x = Arrays.copyOf(denseB.data, denseB.data.length);
		
		// Solve each column
		for (int col = 0; col < b.columns(); col++) {
			double[] bCol = new double[n];
			for (int row = 0; row < n; row++) {
				bCol[row] = denseB.get(row, col);
			}
			double[] xCol = new double[n];
			AccelerateFFI.solveSparseFactorization(pointer, bCol, xCol);
			for (int row = 0; row < n; row++) {
				x[row * b.columns() + col] = xCol[row];
			}
		}
		
		return new DenseMatrix(n, b.columns(), x);
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	@Override
	public void dispose() {
		if (isDisposed)
			return;
		AccelerateFFI.destroySparseFactorization(pointer);
		isDisposed = true;
	}
}

