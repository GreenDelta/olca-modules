package org.openlca.core.math;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.expressions.FormulaInterpreter;

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
	public static TechIndex createProductIndex(
			ProductSystem system, IDatabase db) {
		Exchange refExchange = system.referenceExchange;
		ProcessProduct refFlow = ProcessProduct.of(
				system.referenceProcess, refExchange.flow);
		TechIndex index = new TechIndex(refFlow);
		index.setDemand(ReferenceAmount.get(system));

		// TODO: trove maps?
		Map<Long, ProductSystemDescriptor> systems = new ProductSystemDao(db)
				.getDescriptors().stream()
				.collect(Collectors.toMap(d -> d.getId(), d -> d));
		Map<Long, ProcessDescriptor> processes = new ProcessDao(db)
				.getDescriptors().stream()
				.collect(Collectors.toMap(d -> d.getId(), d -> d));
		Map<Long, FlowDescriptor> flows = new FlowDao(db)
				.getDescriptors().stream()
				.collect(Collectors.toMap(d -> d.getId(), d -> d));
		for (ProcessLink link : system.processLinks) {
			CategorizedDescriptor p = processes.get(link.providerId);
			if (p == null) {
				p = systems.get(link.providerId);
			}
			if (p == null)
				continue;
			FlowDescriptor flow = flows.get(link.flowId);
			if (flow == null)
				continue;
			ProcessProduct provider = ProcessProduct.of(p, flow);
			index.put(provider);
			LongPair exchange = new LongPair(link.processId, link.exchangeId);
			index.putLink(exchange, provider);
		}
		return index;
	}

	public static Inventory createInventory(ProductSystem system,
			MatrixCache cache) {
		TechIndex index = createProductIndex(system, cache.getDatabase());
		AllocationMethod method = AllocationMethod.USE_DEFAULT;
		return Inventory.build(cache, index, method);
	}

	public static Inventory createInventory(ProductSystem system,
			AllocationMethod allocationMethod, MatrixCache cache) {
		TechIndex index = createProductIndex(system, cache.getDatabase());
		return Inventory.build(cache, index, allocationMethod);
	}

	public static Inventory createInventory(
			CalculationSetup setup, MatrixCache cache) {
		ProductSystem system = setup.productSystem;
		AllocationMethod method = setup.allocationMethod;
		if (method == null)
			method = AllocationMethod.NONE;
		TechIndex productIndex = createProductIndex(system,
				cache.getDatabase());
		productIndex.setDemand(ReferenceAmount.get(setup));
		return Inventory.build(cache, productIndex, method);
	}

	public static Set<Long> parameterContexts(CalculationSetup setup,
			TechIndex techIndex) {
		HashSet<Long> set = new HashSet<>();
		if (setup != null && setup.impactMethod != null) {
			set.add(setup.impactMethod.getId());
		}
		if (techIndex != null) {
			set.addAll(techIndex.getProcessIds());
		}
		return set;
	}

	public static FormulaInterpreter interpreter(IDatabase db,
			CalculationSetup setup, TechIndex techIndex) {
		return ParameterTable.interpreter(db,
				parameterContexts(setup, techIndex),
				setup.parameterRedefs);
	}
}
