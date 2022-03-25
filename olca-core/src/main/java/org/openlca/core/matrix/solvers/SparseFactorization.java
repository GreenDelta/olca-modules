package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.julia.Julia;

public class SparseFactorization implements Factorization {

	private final int n;
	private final long pointer;
	private boolean isDisposed;

	private SparseFactorization(int n, long pointer) {
		this.n = n;
		this.pointer = pointer;
	}

	public static SparseFactorization of(CSCMatrix matrix) {
		var pointer = Julia.createSparseFactorization(
				matrix.rows,
				matrix.columnPointers,
				matrix.rowIndices,
				matrix.values);
		return new SparseFactorization(matrix.rows, pointer);
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public double[] solve(double[] b) {
		var x = new double[n];
		Julia.solveSparseFactorization(pointer, b, x);
		return x;
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

	@Override
	public void dispose() {
		if (isDisposed)
			return;
		Julia.destroySparseFactorization(pointer);
		isDisposed = true;
	}
}
