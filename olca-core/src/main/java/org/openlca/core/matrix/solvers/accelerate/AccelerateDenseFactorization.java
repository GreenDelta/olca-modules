package org.openlca.core.matrix.solvers.accelerate;

import java.util.Arrays;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixConverter;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.solvers.Factorization;

/**
 * Dense matrix factorization using Accelerate framework.
 */
public class AccelerateDenseFactorization implements Factorization {

	private final int n;
	private final long pointer;
	private boolean isDisposed;

	private AccelerateDenseFactorization(int n, long pointer) {
		this.n = n;
		this.pointer = pointer;
	}

	public static AccelerateDenseFactorization of(MatrixReader matrix) {
		var dense = MatrixConverter.dense(matrix);
		var n = dense.rows;
		var pointer = AccelerateFFI.createDenseFactorization(dense.rows, dense.data);
		return new AccelerateDenseFactorization(n, pointer);
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public double[] solve(int idx, double d) {
		double[] b = new double[n];
		b[idx] = d;
		AccelerateFFI.solveDenseFactorization(pointer, 1, b);
		return b;
	}

	@Override
	public double[] solve(double[] b) {
		var x = Arrays.copyOf(b, b.length);
		AccelerateFFI.solveDenseFactorization(pointer, 1, x);
		return x;
	}

	@Override
	public Matrix solve(Matrix b) {
		double[] x;
		if (b instanceof DenseMatrix) {
			var data = ((DenseMatrix) b).data;
			x = Arrays.copyOf(data, data.length);
		} else {
			x = MatrixConverter.dense(b).data;
		}
		AccelerateFFI.solveDenseFactorization(pointer, b.columns(), x);
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
		AccelerateFFI.destroyDenseFactorization(pointer);
		isDisposed = true;
	}
}
