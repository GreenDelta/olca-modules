package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowDao extends CategorizedEntityDao<Flow, FlowDescriptor> {

	public FlowDao(IDatabase database) {
		super(Flow.class, FlowDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "version",
				"last_change", "f_category", "flow_type", "f_location",
				"f_reference_flow_property" };
	}

	@Override
	protected FlowDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		FlowDescriptor descriptor = super.createDescriptor(queryResult);
		if (queryResult[7] instanceof String)
			descriptor.setFlowType(FlowType.valueOf((String) queryResult[7]));
		descriptor.setLocation((Long) queryResult[8]);
		Long refProp = (Long) queryResult[9];
		if (refProp != null)
			descriptor.setRefFlowPropertyId(refProp);
		return descriptor;
	}

	/**
	 * Returns the processes where the given flow is an output.
	 */
	public Set<Long> getProviders(long flowId) {
		return getProcessIdsWhereUsed(flowId, false);
	}

	/**
	 * Returns the processes where the given flow is an input.
	 */
	public Set<Long> getRecipients(long flowId) {
		return getProcessIdsWhereUsed(flowId, true);
	}

	private Set<Long> getProcessIdsWhereUsed(long flowId, boolean input) {
		String query = "select f_owner from tbl_exchanges where f_flow = "
				+ flowId + " and is_input = " + (input ? 1 : 0);
		try (Connection con = getDatabase().createConnection()) {
			Statement stmt = con.createStatement();
			ResultSet results = stmt.executeQuery(query);
			Set<Long> ids = new HashSet<>();
			while (results.next())
				ids.add(results.getLong("f_owner"));
			results.close();
			stmt.close();
			return ids;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"failed to load processes for flow " + flowId, e);
			return Collections.emptySet();
		}
	}

	public boolean hasReferenceFactor(long id) {
		return hasReferenceFactor(Collections.singleton(id)).get(id);
	}

	public Map<Long, Boolean> hasReferenceFactor(Set<Long> ids) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id, f_reference_flow_property FROM tbl_flows ");
		query.append("WHERE id IN " + asSqlList(ids));
		query.append(" AND f_reference_flow_property IN ");
		query.append("(SELECT f_flow_property FROM tbl_flow_property_factors WHERE tbl_flows.id = f_flow)");
		Map<Long, Boolean> result = new HashMap<>();
		for (long id : ids)
			result.put(id, false);
		try {
			NativeSql.on(database).query(query.toString(), (res) -> {
				result.put(res.getLong(1), res.getLong(2) != 0l);
				return true;
			});
		} catch (SQLException e) {
			log.error("Error checking for reference factor existence", e);
		}
		return result;
	}

}
