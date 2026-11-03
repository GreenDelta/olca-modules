package org.openlca.core.matrix.solvers.mkl;

import org.openlca.core.matrix.format.CscMatrix;
import org.openlca.core.matrix.solvers.Factorization;

/// A sparse LU-factorization calculated with PARDISO. The native memory of the
/// factorization is released when [#dispose()] is called.
///
/// This class is not thread-safe. A factorization can be used from different
/// threads, but not concurrently, and [#dispose()] must only be called after
/// the last call of [#solve(double\[\])]. Solving with a disposed
/// factorization throws an [IllegalStateException].
class SparseFactorization implements Factorization {

	private final Pardiso pardiso;
	private boolean isDisposed;

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
	public double[] solve(double[] b) {
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
	public void dispose() {
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
