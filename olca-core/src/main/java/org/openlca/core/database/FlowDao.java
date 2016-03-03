package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
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
		return new String[] { "id", "ref_id", "name", "description", "version", "last_change",
				"f_category", "flow_type", "f_location",
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
}
