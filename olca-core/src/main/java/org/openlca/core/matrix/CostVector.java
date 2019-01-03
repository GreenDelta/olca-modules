package org.openlca.core.matrix;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

/**
 * A cost vector contains the unscaled net-costs for a set of process-products.
 * Unscaled means that these net-costs are related to the (allocated) product
 * amount in the respective process. The vector is then scaled with the
 * respective scaling factors in the result calculation.
 */
public final class CostVector {

	private CostVector() {
	}

	public static IMatrix asMatrix(IMatrixSolver solver, double[] values) {
		IMatrix m = solver.matrix(1, values.length);
		for (int col = 0; col < values.length; col++) {
			m.set(0, col, values[col]);
		}
		return m;
	}

	public static double[] build(Inventory inventory, IDatabase db) {
		return new Builder(inventory, db).build();
	}

	private static class Builder {

		private Inventory inventory;
		private CurrencyTable currencyTable;

		private double[] values;

		private Builder(Inventory inventory, IDatabase db) {
			this.inventory = inventory;
			this.currencyTable = CurrencyTable.create(db);
		}

		private double[] build() {
			if (inventory == null || inventory.techIndex == null)
				return new double[0];
			values = new double[inventory.techIndex.size()];
			scan(inventory.technologyMatrix);
			scan(inventory.interventionMatrix);
			return values;
		}

		private void scan(ExchangeMatrix matrix) {
			if (matrix == null)
				return;
			matrix.iterate((row, col, cell) -> {
				double val = cell.getCostValue();
				if (val == 0 || cell.exchange == null) {
					return;
				}
				val = currencyTable.getFactor(cell.exchange.currency) * val;
				values[col] += val;
			});
		}
	}
}
