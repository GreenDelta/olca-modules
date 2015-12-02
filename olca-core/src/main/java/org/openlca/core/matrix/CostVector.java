package org.openlca.core.matrix;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;

/**
 * A cost vector contains the unscaled net-costs for a set of process-products.
 * Unscaled means that these net-costs are related to the (allocated) product
 * amount in the respective process. The vector is then scaled with the
 * respective scaling factors in the result calculation.
 */
public class CostVector {

	public final ProductIndex productIndex;
	public final double[] values;

	CostVector(ProductIndex products, double[] values) {
		this.productIndex = products;
		this.values = values;
	}

	public boolean isEmpty() {
		return productIndex == null || productIndex.size() == 0 || values == null;
	}

	public IMatrix asMatrix(IMatrixFactory<?> factory) {
		IMatrix m = factory.create(1, values.length);
		for(int col = 0; col < values.length; col++) {
			m.setEntry(0, col, values[col]);
		}
		return m;
	}

	public static CostVector build(Inventory inventory, IDatabase db) {
		return new CostMatrixBuilder(inventory, db).build();
	}

	private static class CostMatrixBuilder {

		private Inventory inventory;
		private CurrencyTable currencyTable;

		private double[] values;

		private CostMatrixBuilder(Inventory inventory, IDatabase db) {
			this.inventory = inventory;
			this.currencyTable = CurrencyTable.create(db);
		}

		private CostVector build() {
			if (inventory == null || inventory.getProductIndex() == null)
				return new CostVector(null, null);
			values = new double[inventory.getProductIndex().size()];
			scan(inventory.getTechnologyMatrix());
			scan(inventory.getInterventionMatrix());
			return new CostVector(inventory.getProductIndex(), values);
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
