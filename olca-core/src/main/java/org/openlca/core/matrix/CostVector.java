package org.openlca.core.matrix;

import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

/**
 * A cost vector contains the unscaled net-costs for a set of process-products.
 * Unscaled means that these net-costs are related to the (allocated) product
 * amount in the respective process. The vector is then scaled with the
 * respective scaling factors in the result calculation.
 */
@Deprecated
public final class CostVector {

	private CostVector() {
	}

	@Deprecated
	public static IMatrix asMatrix(IMatrixSolver solver, double[] values) {
		IMatrix m = solver.matrix(1, values.length);
		for (int col = 0; col < values.length; col++) {
			m.set(0, col, values[col]);
		}
		return m;
	}

}
