package org.openlca.core.math;

import org.openlca.core.database.IDatabase;
import org.openlca.core.indices.ExchangeTable;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.matrices.ImpactMatrix;
import org.openlca.core.matrices.ImpactMatrixBuilder;
import org.openlca.core.matrices.Inventory;
import org.openlca.core.matrices.InventoryBuilder;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

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

	static Inventory createInventory(CalculationSetup setup, IDatabase database) {
		ProductSystem system = setup.getProductSystem();
		AllocationMethod method = setup.getAllocationMethod();
		if (method == null)
			method = AllocationMethod.NONE;
		return createInventory(system, method, database);
	}

	/**
	 * Creates the inventory for the given product system.
	 */
	static Inventory createInventory(ProductSystem system,
			AllocationMethod allocationMethod, IDatabase database) {
		ProductIndex productIndex = createProductIndex(system);
		ExchangeTable exchangeTable = new ExchangeTable(database,
				productIndex.getProcessIds());
		FlowIndex flowIndex = new FlowIndex(productIndex, exchangeTable);
		InventoryBuilder inventoryBuilder = new InventoryBuilder(productIndex,
				flowIndex);
		Inventory inventory = inventoryBuilder.build(exchangeTable,
				allocationMethod);
		return inventory;
	}

	/**
	 * Creates a product index from the given product system.
	 * 
	 * TODO: there is currently no check if the system is correctly defined.
	 */
	static ProductIndex createProductIndex(ProductSystem system) {
		Process refProcess = system.getReferenceProcess();
		Exchange refExchange = system.getReferenceExchange();
		Flow refFlow = refExchange.getFlow();
		LongPair refProduct = new LongPair(refProcess.getId(), refFlow.getId());
		double demand = system.getConvertedTargetAmount();
		ProductIndex index = new ProductIndex(refProduct, demand);
		for (ProcessLink link : system.getProcessLinks()) {
			long flow = link.getFlowId();
			long provider = link.getProviderProcessId();
			long recipient = link.getRecipientProcessId();
			LongPair processProduct = new LongPair(provider, flow);
			index.put(processProduct);
			LongPair input = new LongPair(recipient, flow);
			index.putLink(input, processProduct);
		}
		return index;
	}

}
