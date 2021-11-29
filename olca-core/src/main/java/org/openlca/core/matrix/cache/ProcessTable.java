package org.openlca.core.matrix.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.LoggerFactory;

public class ProcessTable {

	private final TLongObjectHashMap<ProcessDescriptor> processes = new TLongObjectHashMap<>();
	private final TLongObjectHashMap<FlowDescriptor> flows = new TLongObjectHashMap<>();

	/**
	 * Maps IDs of product and waste flows to process IDs that have the
	 * respective product as output or waste as input: flow-id ->
	 * provider-process-id. We need this when we build a product system
	 * automatically.
	 */
	private final TLongObjectHashMap<TLongArrayList> flowProviders = new TLongObjectHashMap<>();

	public static ProcessTable create(IDatabase db) {
		return new ProcessTable(db);
	}

	private ProcessTable(IDatabase db) {
		var log = LoggerFactory.getLogger(getClass());
		log.trace("build process index table");

		// index processes and tech-flows
		ProcessDao pDao = new ProcessDao(db);
		for (ProcessDescriptor d : pDao.getDescriptors()) {
			processes.put(d.id, d);
		}
		FlowDao fDao = new FlowDao(db);
		for (FlowDescriptor d : fDao.getDescriptors()) {
			if (d.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			flows.put(d.id, d);
		}

		// index flow -> process relations
		String query = "select f_owner, f_flow, is_input from tbl_exchanges";
		NativeSql.on(db).query(query, r -> {
			long processId = r.getLong(1);
			long flowId = r.getLong(2);
			boolean isInput = r.getBoolean(3);
			FlowDescriptor flow = flows.get(flowId);
			if (flow == null)
				return true;
			FlowType t = flow.flowType;
			if ((isInput && t == FlowType.WASTE_FLOW)
				|| (!isInput && t == FlowType.PRODUCT_FLOW)) {
				TLongArrayList list = flowProviders.get(flowId);
				if (list == null) {
					list = new TLongArrayList();
					flowProviders.put(flowId, list);
				}
				list.add(processId);
			}
			return true;
		});
		log.trace("{} providers mapped", processes.size());
	}

	/**
	 * Returns the process type for the given process-ID.
	 */
	public ProcessType getType(long processId) {
		ProcessDescriptor d = processes.get(processId);
		return d == null ? null : d.processType;
	}

	public TechFlow getProvider(long id, long flowId) {
		var process = processes.get(id);
		var flow = flows.get(flowId);
		if (flow == null || process == null)
			return null;
		return TechFlow.of(process, flow);
	}

	/**
	 * Returns the list of providers that have the flow with the given ID as
	 * product output or waste input.
	 */
	public List<TechFlow> getProviders(long flowId) {
		TLongArrayList list = flowProviders.get(flowId);
		if (list == null)
			return Collections.emptyList();
		FlowDescriptor flow = flows.get(flowId);
		if (flow == null)
			return Collections.emptyList();
		ArrayList<TechFlow> providers = new ArrayList<>();
		list.forEach(id -> {
			var d = processes.get(id);
			if (d != null) {
				providers.add(TechFlow.of(d, flow));
			}
			return true;
		});

		return providers;
	}

	/**
	 * Get all product or waste treatment providers from the database.
	 */
	public List<TechFlow> getProviders() {
		List<TechFlow> list = new ArrayList<>();
		TLongObjectIterator<TLongArrayList> it = flowProviders.iterator();
		while (it.hasNext()) {
			it.advance();
			long flowId = it.key();
			for (long providerId : it.value().toArray()) {
				TechFlow p = getProvider(providerId, flowId);
				if (p != null) {
					list.add(p);
				}
			}
		}
		return list;
	}
}
