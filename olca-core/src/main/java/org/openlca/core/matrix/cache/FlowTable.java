package org.openlca.core.matrix.cache;

import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.util.Categories;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * A simple data structure that holds the flow types of the flows in a database.
 */
public class FlowTable {

	private final TLongObjectHashMap<FlowDescriptor> map = new TLongObjectHashMap<>();

	public static FlowTable create(IDatabase database) {
		return new FlowTable(database);
	}

	private FlowTable(IDatabase database) {
		init(database);
	}

	public void reload(IDatabase db) {
		map.clear();
		init(db);
	}

	private void init(IDatabase db) {
		var dao = new FlowDao(db);
		for (FlowDescriptor d : dao.getDescriptors()) {
			map.put(d.id, d);
		}
	}

	public FlowDescriptor get(long flowID) {
		return map.get(flowID);
	}

	public FlowType type(long flowId) {
		var d = map.get(flowId);
		return d == null ? null : d.flowType;
	}

	/**
	 * Get a map with all `ID -> FlowType` pairs from the database.
	 */
	public static TLongObjectHashMap<FlowType> getTypes(IDatabase db) {
		var types = new TLongObjectHashMap<FlowType>();
		var query = "SELECT id, flow_type FROM tbl_flows";
		NativeSql.on(db).query(query, r -> {
			long flowID = r.getLong(1);
			var typeStr = r.getString(2);
			if (typeStr != null) {
				types.put(flowID, FlowType.valueOf(typeStr));
			}
			return true;
		});
		return types;
	}

	/**
	 * Try to determine the (impact) directions of the given flows. Returns a map
	 * that associates an integer value $v$ to each flow ID. A value $v < 0$ means
	 * that the corresponding flow is an input flow and a value $v > 0$ that it is
	 * an output flow. The larger $|v|$ is the more certain is this classification.
	 * A value of $v = 0$ means that the flow direction cannot be determined from
	 * the information in the database. Also, product and waste flows will always
	 * have a value of $v = 0$ as these flows can always occur on the input or
	 * output side of processes.
	 */
	public static TLongIntHashMap directionsOf(
		IDatabase db, Iterable<FlowDescriptor> flows) {

		// initialize the map
		var map = new TLongIntHashMap();
		if (db == null || flows == null)
			return map;
		for (var flow : flows) {
			if (flow.flowType == FlowType.ELEMENTARY_FLOW) {
				map.put(flow.id, 0);
			}
		}

		// try to get the flow directions from the exchanges
		var sql = "select f_flow, is_input from tbl_exchanges";
		NativeSql.on(db).query(sql, r -> {
			var id = r.getLong(1);
			if (!map.containsKey(id))
				return true;
			var isInput = r.getBoolean(2);
			var current = map.get(id);
			map.put(id, isInput
				? current - 1
				: current + 1);
			return true;
		});

		// check if there are v = 0 cases
		var withZero = new TLongHashSet();
		for (var mapIt = map.iterator(); mapIt.hasNext(); ) {
			mapIt.advance();
			if (mapIt.value() == 0) {
				withZero.add(mapIt.key());
			}
		}
		if (withZero.isEmpty())
			return map;

		// try to determine it from the category path
		var categories = Categories.pathsOf(db);
		for (var flow : flows) {
			if (!withZero.contains(flow.id) || flow.category == null)
				continue;
			var path = categories.pathOf(flow.category);
			if (path == null)
				continue;
			path = path.toLowerCase();
			if (path.contains("resource")) {
				withZero.remove(flow.id);
				map.put(flow.id, -1);
			} else if (path.contains("emission")) {
				withZero.remove(flow.id);
				map.put(flow.id, +1);
			}
		}
		if (withZero.isEmpty())
			return map;

		// try to determine it from other flows that are
		// used in the same impact categories
		var zeroToImpacts = new TLongObjectHashMap<TLongHashSet>();
		var impactTotals = new TLongIntHashMap();
		sql = "select f_impact_category, f_flow from tbl_impact_factors";
		NativeSql.on(db).query(sql, r -> {
			var impact = r.getLong(1);
			var flow = r.getLong(2);

			// add zero-flow impact link
			if (withZero.contains(flow)) {
				var flowImpacts = zeroToImpacts.get(flow);
				if (flowImpacts == null) {
					flowImpacts = new TLongHashSet();
					zeroToImpacts.put(flow, flowImpacts);
				}
				flowImpacts.add(impact);
				return true;
			}

			// add flow direction to impact direction
			var flowVal = map.get(flow);
			if (flowVal != 0) {
				var total = impactTotals.get(impact);
				impactTotals.put(impact, total + flowVal);
			}
			return true;
		});

		for (var it = withZero.iterator(); it.hasNext(); ) {
			var zeroID = it.next();
			var impacts = zeroToImpacts.get(zeroID);
			if (impacts == null)
				continue;
			var val = 0;
			for (var impactIt = impacts.iterator(); impactIt.hasNext(); ) {
				val += impactTotals.get(impactIt.next());
			}
			if (val != 0) {
				map.put(zeroID, val < 0 ? -1 : 1);
			}
		}

		return map;
	}

}
