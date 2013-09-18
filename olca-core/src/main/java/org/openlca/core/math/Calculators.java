package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.FormulaInterpreterBuilder;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactMatrixBuilder;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.LongPair;
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

	static IMatrix createDemandVector(ProductIndex productIndex) {
		LongPair refProduct = productIndex.getRefProduct();
		int idx = productIndex.getIndex(refProduct);
		IMatrix demandVector = MatrixFactory.create(productIndex.size(), 1);
		demandVector.setEntry(idx, 0, productIndex.getDemand());
		return demandVector;
	}

	/**
	 * Creates a matrix with the impact assessment factors for the given method
	 * and flows.
	 */
	static ImpactMatrix createImpactMatrix(ImpactMethodDescriptor method,
			FlowIndex flowIndex, IDatabase database) {
		ImpactMatrixBuilder builder = new ImpactMatrixBuilder(database);
		ImpactMatrix matrix = builder.build(method.getId(), flowIndex);
		return matrix;
	}

	static Inventory createInventory(CalculationSetup setup, MatrixCache cache) {
		ProductSystem system = setup.getProductSystem();
		AllocationMethod method = setup.getAllocationMethod();
		if (method == null)
			method = AllocationMethod.NONE;
		ProductIndex productIndex = ProductSystems.createProductIndex(system);
		InventoryBuilder inventoryBuilder = new InventoryBuilder(cache);
		Inventory inventory = inventoryBuilder.build(productIndex, method);
		FormulaInterpreter interpreter = FormulaInterpreterBuilder.build(
				database, productIndex.getProcessIds());
		FormulaInterpreterBuilder
				.apply(setup.getParameterRedefs(), interpreter);
		inventory.setFormulaInterpreter(interpreter);
		return inventory;
	}

}
