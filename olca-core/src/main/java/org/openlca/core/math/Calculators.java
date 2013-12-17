package org.openlca.core.math;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.FormulaInterpreterBuilder;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.ImpactTableBuilder;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Helper methods for the calculators in this package.
 */
final class Calculators {

	private Calculators() {
	}

	/**
	 * Creates a matrix with the impact assessment factors for the given method
	 * and flows.
	 */
	static ImpactTable createImpactTable(ImpactMethodDescriptor method,
			FlowIndex flowIndex, MatrixCache matrixCache) {
		ImpactTableBuilder builder = new ImpactTableBuilder(matrixCache);
		ImpactTable table = builder.build(method.getId(), flowIndex);
		return table;
	}

	static Inventory createInventory(CalculationSetup setup, MatrixCache cache) {
		ProductSystem system = setup.getProductSystem();
		AllocationMethod method = setup.getAllocationMethod();
		if (method == null)
			method = AllocationMethod.NONE;
		ProductIndex productIndex = ProductSystems.createProductIndex(system);
        productIndex.setDemand(ReferenceAmount.get(setup));
		InventoryBuilder inventoryBuilder = new InventoryBuilder(cache);
		Inventory inventory = inventoryBuilder.build(productIndex, method);
		FormulaInterpreter interpreter = FormulaInterpreterBuilder.build(
				cache.getDatabase(), productIndex.getProcessIds());
		FormulaInterpreterBuilder
				.apply(setup.getParameterRedefs(), interpreter);
		inventory.setFormulaInterpreter(interpreter);
		return inventory;
	}

}
