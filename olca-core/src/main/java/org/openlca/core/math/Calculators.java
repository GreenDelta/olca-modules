package org.openlca.core.math;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;

/**
 * Helper methods for the calculators in this package.
 */
final class Calculators {

	private Calculators() {
	}

	static Inventory createInventory(CalculationSetup setup, MatrixCache cache) {
		ProductSystem system = setup.getProductSystem();
		AllocationMethod method = setup.getAllocationMethod();
		if (method == null)
			method = AllocationMethod.NONE;
		ProductIndex productIndex = ProductSystems.createProductIndex(system);
		productIndex.setDemand(ReferenceAmount.get(setup));
		InventoryBuilder inventoryBuilder = new InventoryBuilder(cache);
		return inventoryBuilder.build(productIndex, method);
	}

	static ParameterTable createParameterTable(IDatabase db,
			CalculationSetup setup, Inventory inventory) {
		Set<Long> contexts = new HashSet<>();
		if (setup.getImpactMethod() != null)
			contexts.add(setup.getImpactMethod().getId());
		if (inventory.getProductIndex() != null)
			contexts.addAll(inventory.getProductIndex().getProcessIds());
		ParameterTable table = ParameterTable.build(db, contexts);
		table.apply(setup.getParameterRedefs());
		return table;
	}

}
