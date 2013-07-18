package org.openlca.core.indices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowTypeIndex {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<Long, FlowType> map = new HashMap<>();

	public FlowTypeIndex(IDatabase database) {
		init(database);
	}

	private void init(IDatabase database) {
		log.trace("initialize flow type index");
		try (Connection con = database.createConnection()) {
			String query = "select id, flow_type from tbl_flows";
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next())
				fetchFlowType(result);
			result.close();
			log.trace("{} flow types fetched", map.size());
		} catch (Exception e) {
			log.error("failed to initialize flow type index", e);
		}
	}

	private void fetchFlowType(ResultSet result) throws Exception {
		long id = result.getLong("id");
		String typeString = result.getString("flow_type");
		if (typeString == null)
			return;
		FlowType type = FlowType.valueOf(typeString);
		map.put(id, type);
	}

	public FlowType getType(long flowId) {
		return map.get(flowId);
	}

}
