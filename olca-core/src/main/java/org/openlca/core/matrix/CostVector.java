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
public class CostVector {

	public final TechIndex productIndex;
	public final double[] values;

	CostVector(TechIndex products, double[] values) {
		this.productIndex = products;
		this.values = values;
	}

	public boolean isEmpty() {
		return productIndex == null || productIndex.size() == 0 || values == null;
	}

	public IMatrix asMatrix(IMatrixSolver solver) {
		IMatrix m = solver.matrix(1, values.length);
		for (int col = 0; col < values.length; col++) {
			m.set(0, col, values[col]);
		}
		return m;
	}

	public static CostVector build(Inventory inventory, IDatabase db) {
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

		private CostVector build() {
			if (inventory == null || inventory.productIndex == null)
				return new CostVector(null, null);
			values = new double[inventory.productIndex.size()];
			scan(inventory.technologyMatrix);
			scan(inventory.interventionMatrix);
			return new CostVector(inventory.productIndex, values);
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
