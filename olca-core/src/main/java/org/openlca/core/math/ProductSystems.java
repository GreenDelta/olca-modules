package org.openlca.core.math;

import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;

/**
 * Provides methods for converting product systems into structures that can be
 * used in calculations and for validation of product systems.
 */
public class ProductSystems {

	private ProductSystems() {
	}

	// TODO: add a validation procedure for product systems

	/**
	 * Creates a product index from the given product system.
	 */
	public static ProductIndex createProductIndex(ProductSystem system) {
		Process refProcess = system.getReferenceProcess();
		Exchange refExchange = system.getReferenceExchange();
		Flow refFlow = refExchange.getFlow();
		LongPair refProduct = new LongPair(refProcess.getId(), refFlow.getId());
		double demand = ReferenceAmount.get(system);
		ProductIndex index = new ProductIndex(refProduct);
        index.setDemand(demand);
		for (ProcessLink link : system.getProcessLinks()) {
			long flow = link.getFlowId();
			long provider = link.getProviderId();
			long recipient = link.getRecipientId();
			LongPair processProduct = new LongPair(provider, flow);
			index.put(processProduct);
			LongPair input = new LongPair(recipient, flow);
			index.putLink(input, processProduct);
		}
		return index;
	}

	public static Inventory createInventory(ProductSystem system,
			MatrixCache matrixCache) {
		ProductIndex index = createProductIndex(system);
		AllocationMethod method = AllocationMethod.USE_DEFAULT;
		return new InventoryBuilder(matrixCache).build(index, method);
	}

	public static Inventory createInventory(ProductSystem system,
			AllocationMethod allocationMethod, MatrixCache matrixCache) {
		ProductIndex index = createProductIndex(system);
		return new InventoryBuilder(matrixCache).build(index, allocationMethod);
	}

}
