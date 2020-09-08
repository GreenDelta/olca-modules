package org.openlca.julia;

import org.openlca.core.matrix.format.CSCMatrix;

public class SparseFactorization {

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

	public double[] solve(int idx, double d) {
		double[] b = new double[n];
		b[idx] = d;
		double[] x = new double[n];
		Julia.solveSparseFactorization(pointer, b, x);
		return x;
	}

	public double[] solve(double[] b) {
		var x = new double[n];
		Julia.solveSparseFactorization(pointer, b, x);
		return x;
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	public void dispose() {
		if (isDisposed)
			return;
		Julia.destroySparseFactorization(pointer);
		isDisposed = true;
	}

}
