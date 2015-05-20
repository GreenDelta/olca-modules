package org.openlca.core.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class JavaSolver implements IMatrixSolver {

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		RealMatrix A = unwrap(a);
		RealVector b = new ArrayRealVector(a.getRowDimension());
		b.setEntry(idx, d);
		RealVector x = new LUDecomposition(A).getSolver().solve(b);
		return x.toArray();
	}
	@Override
	public IMatrix transpose(IMatrix m) {
		return new JavaMatrix(unwrap(m).transpose());
	}

	@Override
	public IMatrix solve(IMatrix a, IMatrix b) {
		RealMatrix A = unwrap(a);
		RealMatrix B = unwrap(b);
		RealMatrix X = new LUDecomposition(A).getSolver().solve(B);
		return new JavaMatrix(X);
	}

	@Override
	public double[] multiply(IMatrix m, double[] v) {
		RealMatrix A = unwrap(m);
		RealMatrix b = new Array2DRowRealMatrix(v.length, 1);
		b.setColumn(0, v);
		return A.multiply(b).getColumn(0);
	}

	@Override
	public IMatrix invert(IMatrix a) {
		RealMatrix _a = unwrap(a);
		RealMatrix inverse = new LUDecomposition(_a).getSolver().getInverse();
		return new JavaMatrix(inverse);
	}

	@Override
	public IMatrix multiply(IMatrix a, IMatrix b) {
		RealMatrix _a = unwrap(a);
		RealMatrix _b = unwrap(b);
		RealMatrix c = _a.multiply(_b);
		return new JavaMatrix(c);
	}

	private RealMatrix unwrap(IMatrix matrix) {
		if (!(matrix instanceof JavaMatrix))
			throw new IllegalArgumentException("unsupported matrix type: "
					+ matrix);
		JavaMatrix javaMatrix = (JavaMatrix) matrix;
		return javaMatrix.getRealMatrix();
	}

	@Override
	public IMatrixFactory<?> getMatrixFactory() {
		return new JavaMatrixFactory();
	}

	@Override
	public void scaleColumns(IMatrix m, double[] v) {
		for (int row = 0; row < m.getRowDimension(); row++) {
			for (int col = 0; col < m.getColumnDimension(); col++) {
				m.setEntry(row, col, v[col] * m.getEntry(row, col));
			}
		}
	}

}
