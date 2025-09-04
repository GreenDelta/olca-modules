package org.openlca.core.matrix.index;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ResultDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ResultDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;

import gnu.trove.map.hash.TLongObjectHashMap;

class TechFlowScan {

	private final IDatabase db;
	private final NativeSql sql;
	private final TLongObjectHashMap<ProcessDescriptor> processes;
	private final Map<Long, FlowDescriptor> flows;
	private boolean withLinkedResults;

	private TechFlowScan(IDatabase db) {
		this.db = db;
		this.sql = NativeSql.on(db);
		this.processes = new ProcessDao(db).descriptorMap();
		flows = new FlowDao(db)
				.getDescriptors(FlowType.PRODUCT_FLOW, FlowType.WASTE_FLOW)
				.stream()
				.collect(Collectors.toMap(d -> d.id, d -> d, (a, b) -> a));
	}

	static TechFlowScan of(IDatabase db) {
		return new TechFlowScan(db);
	}

	TechFlowScan withLinkedResults(boolean b) {
		this.withLinkedResults = b;
		return this;
	}

	void collectInto(TechIndex idx) {
		if (idx == null)
			return;
		scan("select f_owner, f_flow, is_input from tbl_exchanges", processes, idx);
		if (!withLinkedResults)
			return;

		// collect results that are linked as default providers
		var linkedResultIds = new HashSet<Long>();
		sql.query("select f_default_provider from tbl_exchanges " +
				"where default_provider_type = 2", r -> {
			linkedResultIds.add(r.getLong(1));
			return true;
		});
		if (linkedResultIds.isEmpty())
			return;

		var results = new TLongObjectHashMap<ResultDescriptor>();
		new ResultDao(db).getDescriptors(linkedResultIds)
				.forEach(r -> results.put(r.id, r));
		scan("select f_result, f_flow, is_input from tbl_flow_results", results, idx);
	}

	private void scan(
			String query,
			TLongObjectHashMap<? extends RootDescriptor> providers,
			TechIndex idx
	) {
		sql.query(query, r -> {
			long flowId = r.getLong(2);
			var flow = flows.get(flowId);
			if (flow == null)
				return true;
			var type = flow.flowType;
			boolean isInput = r.getBoolean(3);
			if (isInput && type == FlowType.PRODUCT_FLOW)
				return true;
			if (!isInput && type == FlowType.WASTE_FLOW)
				return true;

			long providerId = r.getLong(1);
			var provider = providers.get(providerId);
			if (provider == null) {
				return true;
			}
			idx.add(TechFlow.of(provider, flow));
			return true;
		});
	}
}
