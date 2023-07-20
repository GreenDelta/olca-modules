package org.openlca.core.matrix.solvers.mkl;

import org.openlca.core.matrix.solvers.Factorization;

import java.util.concurrent.atomic.AtomicBoolean;

class SparseFactorization implements Factorization  {

	private final long pointer;
	private final int size;
	private final AtomicBoolean isDisposed;

	SparseFactorization(long pointer, int size) {
		this.pointer = pointer;
		this.size = size;
		this.isDisposed = new AtomicBoolean(false);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public double[] solve(double[] b) {
		double[] x = new double[size];
		if (b == null)
			return x;
		int info = MKL.solveSparseFactorization(pointer, b, x);
		InfoCode.checkPardiso(info);
		return x;
	}

	@Override
	public void dispose() {
		if (isDisposed.get())
			return;
		synchronized (isDisposed) {
			if (isDisposed.get())
				return;
			MKL.disposeSparseFactorization(pointer);
		}
	}

	@Override
	public boolean isDisposed() {
		return isDisposed.get();
	}
}
