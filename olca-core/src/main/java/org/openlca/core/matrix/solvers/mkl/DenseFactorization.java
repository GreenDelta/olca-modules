package org.openlca.core.matrix.solvers.mkl;

import java.util.Arrays;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.solvers.Factorization;

/// A dense LU-factorization calculated with LAPACK. The factorization is stored
/// in Java arrays, so there is no native memory that would need to be released.
class DenseFactorization implements Factorization {

	private final int n;
	private final double[] lu;
	private final long[] pivots;
	private volatile boolean isDisposed;

	private DenseFactorization(int n, double[] lu, long[] pivots) {
		this.n = n;
		this.lu = lu;
		this.pivots = pivots;
	}

	static DenseFactorization of(DenseMatrix matrix) {
		int n = matrix.rows();
		var lu = Arrays.copyOf(matrix.data, matrix.data.length);
		var pivots = new long[n];
		int info = MKL.luFactorization(n, lu, pivots);
		InfoCode.checkBlas(info);
		return new DenseFactorization(n, lu, pivots);
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public double[] solve(double[] b) {
		if (isDisposed)
			throw new IllegalStateException("factorization was disposed");
		if (b == null)
			return new double[n];
		var x = Arrays.copyOf(b, b.length);
		int info = MKL.luSolve(n, 1, lu, pivots, x);
		InfoCode.checkBlas(info);
		return x;
	}

	@Override
	public void dispose() {
		isDisposed = true;
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}
}
