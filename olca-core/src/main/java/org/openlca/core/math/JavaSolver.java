package org.openlca.core.math;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class JavaSolver implements ISolver {

	@Override
	public IMatrix solve(IMatrix a, IMatrix b) {
		RealMatrix _a = unwrap(a);
		RealMatrix _b = unwrap(b);
		RealMatrix x = new LUDecomposition(_a).getSolver().solve(_b);
		return new JavaMatrix(x);
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
