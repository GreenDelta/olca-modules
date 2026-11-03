package org.openlca.core.matrix.solvers.mkl;

import org.openlca.core.matrix.format.CscMatrix;
import org.openlca.core.matrix.solvers.Factorization;

/// A sparse LU-factorization calculated with PARDISO. The native memory of the
/// factorization is released when [#dispose()] is called. The methods of this
/// class are synchronized because PARDISO instances are not thread-safe and the
/// native memory must not be released while a solve call is running.
class SparseFactorization implements Factorization {

	private final Pardiso pardiso;
	private volatile boolean isDisposed;

	private SparseFactorization(Pardiso pardiso) {
		this.pardiso = pardiso;
	}

	static SparseFactorization of(CscMatrix matrix) {
		var pardiso = Pardiso.of(
			matrix.rows,
			matrix.values,
			matrix.rowIndices,
			matrix.columnPointers);
		try {
			InfoCode.checkPardiso(pardiso.factorize());
			return new SparseFactorization(pardiso);
		} catch (RuntimeException e) {
			pardiso.close();
			throw e;
		}
	}

	@Override
	public int size() {
		return pardiso.size();
	}

	@Override
	public synchronized double[] solve(double[] b) {
		if (isDisposed)
			throw new IllegalStateException("factorization was disposed");
		double[] x = new double[size()];
		if (b == null)
			return x;
		int info = pardiso.solve(b, x);
		InfoCode.checkPardiso(info);
		return x;
	}

	@Override
	public synchronized void dispose() {
		if (isDisposed)
			return;
		isDisposed = true;
		pardiso.close();
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}
}
