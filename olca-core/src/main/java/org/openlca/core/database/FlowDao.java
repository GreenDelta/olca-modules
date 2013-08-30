package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

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

	public List<ProcessDescriptor> getProviders(FlowDescriptor descriptor) {
		List<Long> processIds = getProcessIdsWhereUsed(descriptor, false);
		return loadProcessDescriptors(processIds);
	}

	public List<ProcessDescriptor> getRecipients(FlowDescriptor descriptor) {
		List<Long> processIds = getProcessIdsWhereUsed(descriptor, true);
		return loadProcessDescriptors(processIds);
	}

	private List<Long> getProcessIdsWhereUsed(FlowDescriptor descriptor,
			boolean input) {
		if (descriptor == null)
			return Collections.emptyList();
		String jpql = "select p.id from Process p join p.exchanges e "
				+ "where e.flow.id = :flowId and e.input = :input ";
		Map<String, Object> params = new HashMap<>();
		params.put("flowId", descriptor.getId());
		params.put("input", input);
		return query().getAll(Long.class, jpql, params);
	}

	private List<ProcessDescriptor> loadProcessDescriptors(List<Long> processIds) {
		if (processIds == null || processIds.isEmpty())
			return Collections.emptyList();
		// TODO: performance may could be improved if we query the
		// database with an 'IN - query'
		List<ProcessDescriptor> results = new ArrayList<>();
		ProcessDao dao = new ProcessDao(getDatabase());
		for (Long processId : processIds) {
			ProcessDescriptor d = dao.getDescriptor(processId);
			if (d != null)
				results.add(d);
		}
		return results;
	}

}
