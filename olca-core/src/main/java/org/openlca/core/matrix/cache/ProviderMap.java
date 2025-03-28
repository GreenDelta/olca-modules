package org.openlca.core.matrix.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ResultDao;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.slf4j.LoggerFactory;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

/// Contains the product and waste flow providers from the database. Currently,
/// this only contains processes and results but not product systems, as
/// linking them automatically is not supported yet.
public class ProviderMap {

	private final TLongObjectHashMap<RootDescriptor> providers = new TLongObjectHashMap<>();
	private final TLongObjectHashMap<FlowDescriptor> flows = new TLongObjectHashMap<>();

	/**
	 * Maps IDs of product and waste flows to process IDs that have the
	 * respective product as output or waste as input: flow-id ->
	 * provider-process-id. We need this when we build a product system
	 * automatically.
	 */
	private final TLongObjectHashMap<TLongArrayList> flowProviders = new TLongObjectHashMap<>();

	public static ProviderMap create(IDatabase db) {
		return new ProviderMap(db);
	}

	private ProviderMap(IDatabase db) {
		var log = LoggerFactory.getLogger(getClass());
		log.trace("build provider map");

		// index processes, results, and flows
		for (var d : new ProcessDao(db).getDescriptors()) {
			providers.put(d.id, d);
		}
		for (var r : new ResultDao(db).getDescriptors()) {
			providers.put(r.id, r);
		}
		for (var d : new FlowDao(db).getDescriptors()) {
			if (d.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			flows.put(d.id, d);
		}

		// index flow -> providers
		mapFlowProviders(db,
				"select f_owner, f_flow, is_input from tbl_exchanges");
		mapFlowProviders(db,
				"select f_result, f_flow, is_input from tbl_flow_results");

		log.trace("{} providers mapped", providers.size());
	}

	private void mapFlowProviders(IDatabase db, String q) {
		NativeSql.on(db).query(q, r -> {
			long providerId = r.getLong(1);
			long flowId = r.getLong(2);
			boolean isInput = r.getBoolean(3);
			var flow = flows.get(flowId);
			if (skip(flow, isInput))
				return true;
			var list = flowProviders.get(flowId);
			if (list == null) {
				list = new TLongArrayList();
				flowProviders.put(flowId, list);
			}
			list.add(providerId);
			return true;
		});
	}

	private boolean skip(FlowDescriptor flow, boolean isInput) {
		if (flow == null || flow.flowType == null)
			return true;
		return switch (flow.flowType) {
			case PRODUCT_FLOW -> isInput;
			case WASTE_FLOW -> !isInput;
			default -> true;
		};
	}

	/**
	 * Returns the process type for the given process-ID.
	 */
	public RootDescriptor getType(long processId) {
		ProcessDescriptor d = providers.get(processId);
		return d == null ? null : d.processType;
	}

	/**
	 * Returns the list of providers that have the flow with the given ID as
	 * product output or waste input.
	 */
	public List<TechFlow> getProvidersOf(long flowId) {
		var list = flowProviders.get(flowId);
		if (list == null)
			return Collections.emptyList();
		var flow = flows.get(flowId);
		if (flow == null)
			return Collections.emptyList();

		var providers = new ArrayList<TechFlow>();
		list.forEach(id -> {
			var d = this.providers.get(id);
			if (d != null) {
				providers.add(TechFlow.of(d, flow));
			}
			return true;
		});
		return providers;
	}

	public TechFlow get(long providerId, long flowId) {
		var process = providers.get(providerId);
		var flow = flows.get(flowId);
		if (flow == null || process == null)
			return null;
		return TechFlow.of(process, flow);
	}

	/// Get all product or waste treatment providers from the database.
	public List<TechFlow> getAll() {
		var list = new ArrayList<TechFlow>();
		var it = flowProviders.iterator();
		while (it.hasNext()) {
			it.advance();
			long flowId = it.key();
			for (long providerId : it.value().toArray()) {
				TechFlow p = get(providerId, flowId);
				if (p != null) {
					list.add(p);
				}
			}
		}
		return list;
	}
}
