package org.openlca.core.matrix.cache;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * A simple data structure that holds the flow types of the flows in a database.
 */
public class FlowTable {

	private final Logger log = LoggerFactory.getLogger(getClass());

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
		log.trace("initialize flow index");
		FlowDao dao = new FlowDao(db);
		var flows = dao.getDescriptors();
		for (FlowDescriptor d : flows) {
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

	/** Get the IDs of all flows in this table. */
	public long[] getFlowIds() {
		return map.keys();
	}

	/**
	 * Get a map with all `ID -> FlowType` pairs from the database.
	 */
	public static TLongObjectHashMap<FlowType> getTypes(IDatabase db) {
		var types = new TLongObjectHashMap<FlowType>();
		try {
			var query = "SELECT id, flow_type FROM tbl_flows";
			NativeSql.on(db).query(query, r -> {
				long flowID = r.getLong(1);
				var typeStr = r.getString(2);
				if (typeStr != null) {
					types.put(flowID, FlowType.valueOf(typeStr));
				}
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException("failed to load flow types", e);
		}
		return types;
	}
}
