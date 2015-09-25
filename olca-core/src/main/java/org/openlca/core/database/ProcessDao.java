package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ProcessDao extends
		CategorizedEntityDao<Process, ProcessDescriptor> {

	public ProcessDao(IDatabase database) {
		super(Process.class, ProcessDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "f_category",
				"process_type", "infrastructure_process",
				"f_location", "f_quantitative_reference" };
	}

	@Override
	protected ProcessDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		ProcessDescriptor d = super.createDescriptor(queryResult);
		d.setProcessType(ProcessType.valueOf((String) queryResult[5]));
		d.setInfrastructureProcess((Integer) queryResult[6] == 1);
		d.setLocation((Long) queryResult[7]);
		d.setQuantitativeReference((Long) queryResult[8]);
		return d;
	}

	public List<FlowDescriptor> getTechnologyInputs(ProcessDescriptor descriptor) {
		List<Long> flowIds = getTechnologies(descriptor, true);
		return loadFlowDescriptors(flowIds);
	}

	public List<FlowDescriptor> getTechnologyOutputs(
			ProcessDescriptor descriptor) {
		List<Long> flowIds = getTechnologies(descriptor, false);
		return loadFlowDescriptors(flowIds);
	}

	private List<Long> getTechnologies(ProcessDescriptor descriptor,
			boolean input) {
		if (descriptor == null)
			return Collections.emptyList();
		String sql = "select f_flow from tbl_exchanges where f_owner = "
				+ descriptor.getId() + " and is_input = " + (input ? 1 : 0);
		List<Long> ids = new ArrayList<>();
		try (Connection con = getDatabase().createConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(sql)) {
			while (rs.next())
				ids.add(rs.getLong("f_flow"));
			return ids;
		} catch (SQLException e) {
			log.error("Error loading technologies", e);
			return Collections.emptyList();
		}
	}

	private List<FlowDescriptor> loadFlowDescriptors(List<Long> flowIds) {
		if (flowIds == null || flowIds.isEmpty())
			return Collections.emptyList();
		// TODO: performance may could be improved if we query the
		// database with an 'IN - query'
		List<FlowDescriptor> results = new ArrayList<>();
		FlowDao dao = new FlowDao(getDatabase());
		for (Long flowId : flowIds) {
			FlowDescriptor d = dao.getDescriptor(flowId);
			if (d != null)
				results.add(d);
		}
		return results;
	}

}
