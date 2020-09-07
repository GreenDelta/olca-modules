package org.openlca.julia;

import java.util.Arrays;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.MatrixConverter;

public class DenseFactorization {

	private final int n;
	private final long pointer;
	private boolean isDisposed;

	private DenseFactorization(int n, long pointer) {
		this.n = n;
		this.pointer = pointer;
	}

	public static DenseFactorization of(IMatrix matrix) {
		var dense = MatrixConverter.dense(matrix);
		var n = dense.rows;
		var pointer = Julia.createDenseFactorization(
				dense.rows, dense.data);
		return new DenseFactorization(n, pointer);
	}

	public double[] solve(int idx, double d) {
		double[] b = new double[n];
		b[idx] = d;
		Julia.solveDenseFactorization(pointer, 1, b);
		return b;
	}

	public double[] solve(DenseFactorization f, double[] b) {
		var x = Arrays.copyOf(b, b.length);
		Julia.solveDenseFactorization(pointer, 1, x);
		return x;
	}

	public double[] solve(DenseFactorization f, IMatrix b) {
		double[] x;
		if (b instanceof DenseMatrix) {
			var data = ((DenseMatrix) b).data;
			x = Arrays.copyOf(data, data.length);
		} else {
			x = MatrixConverter.dense(b).data;
		}
		Julia.solveDenseFactorization(pointer, b.columns(), x);
		return x;
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	public void dispose() {
		if (isDisposed)
			return;
		Julia.destroyDenseFactorization(pointer);
		isDisposed = true;
	}
}
