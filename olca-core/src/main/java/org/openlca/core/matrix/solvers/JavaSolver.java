package org.openlca.core.matrix.solvers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.format.MatrixReader;

public class JavaSolver implements MatrixSolver {

	@Override
	public boolean hasSparseSupport() {
		return false;
	}

	@Override
	public Matrix matrix(int rows, int columns) {
		return new JavaMatrix(rows, columns);
	}

	@Override
	public double[] solve(MatrixReader a, int idx, double d) {
		var A = unwrap(a);
		var b = new ArrayRealVector(a.rows());
		b.setEntry(idx, d);
		var x = new LUDecomposition(A).getSolver().solve(b);
		return x.toArray();
	}

	@Override
	public double[] multiply(MatrixReader m, double[] v) {
		var A = unwrap(m);
		var b = new Array2DRowRealMatrix(v.length, 1);
		b.setColumn(0, v);
		return A.multiply(b).getColumn(0);
	}

	@Override
	public Matrix invert(MatrixReader a) {
		var _a = unwrap(a);
		var inverse = new LUDecomposition(_a).getSolver().getInverse();
		return new JavaMatrix(inverse);
	}

	@Override
	public Matrix multiply(MatrixReader a, MatrixReader b) {
		RealMatrix _a = unwrap(a);
		RealMatrix _b = unwrap(b);
		RealMatrix c = _a.multiply(_b);
		return new JavaMatrix(c);
	}

	private static RealMatrix unwrap(MatrixReader m) {
		if (m instanceof JavaMatrix)
			return ((JavaMatrix) m).getRealMatrix();
		var rm = new Array2DRowRealMatrix(
				m.rows(), m.columns());
		m.iterate(rm::setEntry);
		return rm;
	}

	@Override
	public Factorization factorize(MatrixReader matrix) {
		return LU.of(matrix);
	}

	private static class LU implements Factorization {

		private final int n;
		private final LUDecomposition lu;
		private boolean disposed;

		LU(int n, LUDecomposition lu) {
			this.n = n;
			this.lu = lu;
		}

		static LU of (MatrixReader matrix) {
			var m = unwrap(matrix);
			var lu = new LUDecomposition(m);
			return new LU(matrix.rows(), lu);
		}

		@Override
		public int size() {
			return n;
		}

		@Override
		public double[] solve(double[] b) {
			var vec = new ArrayRealVector(b);
			var x = lu.getSolver().solve(vec);
			return x.toArray();
		}

		@Override
		public Matrix solve(Matrix b) {
			var _b = unwrap(b);
			var x = lu.getSolver().solve(_b);
			return new JavaMatrix(x);
		}

		@Override
		public void dispose() {
			disposed = true;
		}

		@Override
		public boolean isDisposed() {
			return disposed;
		}
	}
}
