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
import org.openlca.core.model.descriptors.RootDescriptor;
import org.slf4j.LoggerFactory;

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

/// A map for getting the possible providers of product inputs or
/// waste outputs quickly. Currently, it only contains processes and
/// results but not product systems, as linking them automatically is
/// not fully supported yet.
public class ProviderMap {

	private final TLongObjectHashMap<RootDescriptor> providers = new TLongObjectHashMap<>();
	private final TLongObjectHashMap<FlowDescriptor> flows = new TLongObjectHashMap<>();

	/// Maps the IDs of product and waste flows to the IDs of possible
	/// providers.
	private final TLongObjectHashMap<TLongArrayList> map = new TLongObjectHashMap<>();

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
		mapProviders(db,
				"select f_owner, f_flow, is_input from tbl_exchanges");
		mapProviders(db,
				"select f_result, f_flow, is_input from tbl_flow_results");

		log.trace("{} providers mapped", providers.size());
	}

	private void mapProviders(IDatabase db, String q) {
		NativeSql.on(db).query(q, r -> {
			long providerId = r.getLong(1);
			long flowId = r.getLong(2);
			boolean isInput = r.getBoolean(3);
			var flow = flows.get(flowId);
			if (skip(flow, isInput))
				return true;
			var list = map.get(flowId);
			if (list == null) {
				list = new TLongArrayList();
				map.put(flowId, list);
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

	/// Returns the list of possible providers for the flow with the
	/// given ID.
	public List<TechFlow> getProvidersOf(long flowId) {
		var list = map.get(flowId);
		if (list == null)
			return Collections.emptyList();
		var flow = flows.get(flowId);
		if (flow == null)
			return Collections.emptyList();

		var techFlows = new ArrayList<TechFlow>();
		for (var pid = list.iterator(); pid.hasNext(); ) {
			var provider = providers.get(pid.next());
			if (provider != null) {
				techFlows.add(TechFlow.of(provider, flow));
			}
		}
		return techFlows;
	}

	/// Returns the tech-flow for the provider and flow ID. Returns `null`
	/// when there is no such combination in this map.
	public TechFlow getTechFlow(long providerId, long flowId) {
		var provider = providers.get(providerId);
		var flow = flows.get(flowId);
		return flow != null && provider != null
				? TechFlow.of(provider, flow)
				: null;
	}

	/// Get all possible provider-flow pairs from the database.
	public List<TechFlow> getTechFlows() {
		var list = new ArrayList<TechFlow>();
		var it = map.iterator();
		while (it.hasNext()) {
			it.advance();
			long flowId = it.key();
			for (var pid = it.value().iterator(); pid.hasNext(); ) {
				TechFlow p = getTechFlow(pid.next(), flowId);
				if (p != null) {
					list.add(p);
				}
			}
		}
		return list;
	}
}
