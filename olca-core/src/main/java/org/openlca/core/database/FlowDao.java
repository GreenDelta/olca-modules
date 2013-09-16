package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowDao extends CategorizedEntityDao<Flow, FlowDescriptor> {

	public FlowDao(IDatabase database) {
		super(Flow.class, FlowDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description",
				"flow_type", "f_category", "f_location",
				"f_reference_flow_property" };
	}

	@Override
	protected FlowDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		FlowDescriptor descriptor = super.createDescriptor(queryResult);
		if (queryResult[4] instanceof String)
			descriptor.setFlowType(FlowType.valueOf((String) queryResult[4]));
		descriptor.setCategory((Long) queryResult[5]);
		descriptor.setLocation((Long) queryResult[6]);
		Long refProp = (Long) queryResult[7];
		if (refProp != null)
			descriptor.setRefFlowPropertyId(refProp);
		return descriptor;
	}

	/**
	 * Returns the processes where the given flow is an output.
	 */
	public List<Long> getProviders(long flowId) {
		return getProcessIdsWhereUsed(flowId, false);
	}

	/**
	 * Returns the processes where the given flow is an input.
	 */
	public List<Long> getRecipients(long flowId) {
		return getProcessIdsWhereUsed(flowId, true);
	}

	private List<Long> getProcessIdsWhereUsed(long flowId, boolean input) {
		String query = "select f_owner from tbl_exchanges where f_flow = "
				+ flowId + " and is_input = " + (input ? 1 : 0);
		try (Connection con = getDatabase().createConnection()) {
			Statement stmt = con.createStatement();
			ResultSet results = stmt.executeQuery(query);
			List<Long> ids = new ArrayList<>();
			while (results.next())
				ids.add(results.getLong("f_owner"));
			results.close();
			stmt.close();
			return ids;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"failed to load processes for flow " + flowId, e);
			return Collections.emptyList();
		}
	}
}
