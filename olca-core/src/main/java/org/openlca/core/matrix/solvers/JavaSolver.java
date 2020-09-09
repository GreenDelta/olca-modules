package org.openlca.core.matrix.solvers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.format.JavaMatrix;

public class JavaSolver implements IMatrixSolver {

	@Override
	public IMatrix matrix(int rows, int columns) {
		return new JavaMatrix(rows, columns);
	}

	@Override
	public double[] solve(IMatrix a, int idx, double d) {
		var A = unwrap(a);
		var b = new ArrayRealVector(a.rows());
		b.setEntry(idx, d);
		var x = new LUDecomposition(A).getSolver().solve(b);
		return x.toArray();
	}

	@Override
	public double[] multiply(IMatrix m, double[] v) {
		var A = unwrap(m);
		var b = new Array2DRowRealMatrix(v.length, 1);
		b.setColumn(0, v);
		return A.multiply(b).getColumn(0);
	}

	@Override
	public IMatrix invert(IMatrix a) {
		var _a = unwrap(a);
		var inverse = new LUDecomposition(_a).getSolver().getInverse();
		return new JavaMatrix(inverse);
	}

	@Override
	public IMatrix multiply(IMatrix a, IMatrix b) {
		RealMatrix _a = unwrap(a);
		RealMatrix _b = unwrap(b);
		RealMatrix c = _a.multiply(_b);
		return new JavaMatrix(c);
	}

	private RealMatrix unwrap(IMatrix m) {
		if (m instanceof JavaMatrix)
			return ((JavaMatrix) m).getRealMatrix();
		var rm = new Array2DRowRealMatrix(
				m.rows(), m.columns());
		m.iterate(rm::setEntry);
		return rm;
	}
}
