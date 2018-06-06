package org.openlca.julia;

public class Umfpack {

	public static double[] solve(UmfMatrix m, double[] demand) {
		double[] result = new double[demand.length];
		Julia.umfSolve(m.rowCount,
				m.columnPointers,
				m.rowIndices,
				m.values,
				demand,
				result);
		return result;
	}

	public static UmfFactorizedMatrix factorize(UmfMatrix m) {
		long pointer = Julia.umfFactorize(
				m.rowCount,
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
