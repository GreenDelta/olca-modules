package org.openlca.julia;

import org.openlca.core.matrix.format.CCRMatrix;

public class Umfpack {

	public static double[] solve(CCRMatrix m, double[] demand) {
		double[] result = new double[demand.length];
		Julia.umfSolve(m.rows,
				m.columnPointers,
				m.rowIndices,
				m.values,
				demand,
				result);
		return result;
	}

	public static UmfFactorizedMatrix factorize(CCRMatrix m) {
		long pointer = Julia.umfFactorize(
				m.rows,
				m.columnPointers,
				m.rowIndices,
				m.values);
		return new UmfFactorizedMatrix(pointer);
	}

	public static double[] solve(UmfFactorizedMatrix m, double[] demand) {
		double[] result = new double[demand.length];
		Julia.umfSolveFactorized(m.pointer, demand, result);
		return result;
	}
}
