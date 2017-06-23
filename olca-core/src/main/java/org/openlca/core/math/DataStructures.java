package org.openlca.core.math;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;

/**
 * Provides helper methods for creating matrix-like data structures that can be
 * used in calculations (but also exports, validations, etc.).
 */
public class DataStructures {

	private DataStructures() {
	}

	/**
	 * Creates a product index from the given product system.
	 */
	public static TechIndex createProductIndex(ProductSystem system) {
		long providerId = system.getReferenceProcess().getId();
		Exchange refExchange = system.getReferenceExchange();
		long flowId = refExchange.flow.getId();
		LongPair refFlow = new LongPair(providerId, flowId);
		TechIndex index = new TechIndex(refFlow);
		index.setDemand(ReferenceAmount.get(system));
		for (ProcessLink link : system.getProcessLinks()) {
			LongPair provider = new LongPair(link.providerId, link.flowId);
			index.put(provider);
			LongPair exchange = new LongPair(link.processId, link.exchangeId);
			index.putLink(exchange, provider);
		}
		return index;
	}

	public static Inventory createInventory(ProductSystem system,
			MatrixCache matrixCache) {
		TechIndex index = createProductIndex(system);
		AllocationMethod method = AllocationMethod.USE_DEFAULT;
		return Inventory.build(matrixCache, index, method);
	}

	public static Inventory createInventory(ProductSystem system,
			AllocationMethod allocationMethod, MatrixCache matrixCache) {
		TechIndex index = createProductIndex(system);
		return Inventory.build(matrixCache, index, allocationMethod);
	}

	public static Inventory createInventory(CalculationSetup setup,
			MatrixCache cache) {
		ProductSystem system = setup.productSystem;
		AllocationMethod method = setup.allocationMethod;
		if (method == null)
			method = AllocationMethod.NONE;
		TechIndex productIndex = createProductIndex(system);
		productIndex.setDemand(ReferenceAmount.get(setup));
		return Inventory.build(cache, productIndex, method);
	}

	public static ParameterTable createParameterTable(IDatabase db,
			CalculationSetup setup, Inventory inventory) {
		Set<Long> contexts = new HashSet<>();
		if (setup.impactMethod != null)
			contexts.add(setup.impactMethod.getId());
		if (inventory.productIndex != null)
			contexts.addAll(inventory.productIndex.getProcessIds());
		ParameterTable table = ParameterTable.build(db, contexts);
		table.apply(setup.parameterRedefs);
		return table;
	}

}
