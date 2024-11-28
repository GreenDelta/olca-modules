package org.openlca.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowReplacer {

	/// Get the used flows from the database. This includes flows from processes,
	/// impact categories, results, and EPDs. Note that maybe not all returned
	/// flows can be replaced, because they are maybe only used in library data.
	public static List<FlowDescriptor> getUsedFlowsOf(IDatabase db) {
		if (db == null)
			return Collections.emptyList();

		var tables = new String[]{
				"tbl_exchanges",
				"tbl_impact_factors",
				"tbl_flow_results",
				"tbl_epds"
		};
		var ids = new HashSet<Long>();
		for (var table : tables) {
			var q = "select distinct f_flow from " + table;
			NativeSql.on(db).query(q, r -> {
				ids.add(r.getLong(1));
				return true;
			});
		}

		return !ids.isEmpty()
				? new FlowDao(db).getDescriptors(ids)
				: Collections.emptyList();
	}

	/// Returns a list of flows with which the given flow could be replaced, in
	/// principle. This is the case when the flows have the same type and a
	/// common flow property. We further restrict the second condition so that
	/// the flows have exactly the same set of flow properties as the replacer
	/// is currently not smart enough to handle all conversions.
	public static List<FlowDescriptor> getCandidatesOf(
			IDatabase db, FlowDescriptor flow
	) {
		if (db == null || flow == null || flow.flowType == null)
			return Collections.emptyList();

		var sql = NativeSql.on(db);

		// a flow is a candidate of f if it has all flow properties
		// of f. We check this with simple check-sum.
		var props = new HashMap<Long, Integer>();
		var checkRef = new AtomicInteger(0);
		var propQ = "select distinct f_flow_property from " +
				"tbl_flow_property_factors where f_flow = " + flow.id;
		sql.query(propQ, r -> {
			props.put(r.getLong(1), checkRef.incrementAndGet());
			return true;
		});
		int checkSum = checkRef.get();
		if (props.isEmpty())
			return Collections.emptyList();

		var sums = new HashMap<Long, Integer>();
		var candidates = new HashSet<Long>();
		var candQ = """
				select distinct flow.id, fac.f_flow_property
				  from tbl_flows flow
				  inner join tbl_flow_property_factors fac
				  on flow.id = fac.f_flow
				  where flow.flow_type = '"""
				+ flow.flowType.name() + "'";
		sql.query(candQ, r -> {
			var propId = r.getLong(2);
			var i = props.get(propId);
			if (i == null)
				return true;
			var flowId = r.getLong(1);
			if (flowId == flow.id)
				return true;
			var sum = sums.compute(flowId, ($, old) -> old == null ? i : old + i);
			if (sum == checkSum) {
				candidates.add(flowId);
			}
			return true;
		});

		return candidates.isEmpty()
				? Collections.emptyList()
				: new FlowDao(db).getDescriptors(candidates);
	}
}
