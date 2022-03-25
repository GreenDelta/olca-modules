package org.openlca.core.matrix.solvers;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.julia.Julia;

public class Umfpack {

	public static double[] solve(CSCMatrix m, double[] demand) {
		double[] result = new double[demand.length];
		Julia.umfSolve(m.rows,
				m.columnPointers,
				m.rowIndices,
				m.values,
				demand,
				result);
		return result;
	}

	public static UmfFactorizedMatrix factorize(CSCMatrix m) {
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
