package org.openlca.core.matrix.solvers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.JavaMatrix;

public class JavaSolver implements IMatrixSolver {

	@Override
	public IMatrix matrix(int rows, int columns) {
		return new JavaMatrix(rows, columns);
	}

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		RealMatrix A = unwrap(a);
		RealVector b = new ArrayRealVector(a.rows());
		b.setEntry(idx, d);
		RealVector x = new LUDecomposition(A).getSolver().solve(b);
		return x.toArray();
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

}
