package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Flow;
import org.openlca.core.model.lean.BaseDescriptor;
import org.openlca.core.model.lean.FlowDescriptor;

public class FlowDao extends CategorizedEnitityDao<Flow> {

	public FlowDao(EntityManagerFactory factory) {
		super(Flow.class, factory);
	}

	@Override
	protected String getDescriptorQuery() {
		return "select e.id, e.name, e.description, loc.code from Flow e "
				+ "left join e.location loc ";
	}

	@Override
	protected BaseDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		FlowDescriptor descriptor = new FlowDescriptor();
		try {
			descriptor.setId((Long) queryResult[0]);
			descriptor.setName((String) queryResult[1]);
			descriptor.setDescription((String) queryResult[2]);
			descriptor.setLocationCode((String) queryResult[3]);
		} catch (Exception e) {
			log.error("failed to map values to flow descriptor", e);
		}
		return descriptor;
	}

	public List<BaseDescriptor> getProviders(Flow flow) throws Exception {
		List<Long> processIds = getProcessIdsWhereUsed(flow, false);
		return loadProcessDescriptors(processIds);
	}

	public List<BaseDescriptor> getRecipients(Flow flow) throws Exception {
		List<Long> processIds = getProcessIdsWhereUsed(flow, true);
		return loadProcessDescriptors(processIds);
	}

	private List<Long> getProcessIdsWhereUsed(Flow flow, boolean input)
			throws Exception {
		if (flow == null)
			return Collections.emptyList();
		String jpql = "select p.id from Process p join p.exchanges e "
				+ "where e.flow.id = :flowId and e.input = :input ";
		Map<String, Object> params = new HashMap<>();
		params.put("flowId", flow.getId());
		params.put("input", input);
		return query().getAll(Long.class, jpql, params);
	}

	private List<BaseDescriptor> loadProcessDescriptors(List<Long> processIds)
			throws Exception {
		if (processIds == null || processIds.isEmpty())
			return Collections.emptyList();
		// TODO: performance may could be improved if we query the
		// database with an 'IN - query'
		List<BaseDescriptor> results = new ArrayList<>();
		ProcessDao dao = new ProcessDao(getEntityFactory());
		for (Long processId : processIds) {
			BaseDescriptor d = dao.getDescriptor(processId);
			if (d != null)
				results.add(d);
		}
		return results;
	}

	public List<BaseDescriptor> whereUsed(Flow flow) {
		return new FlowUseSearch(getEntityFactory()).findUses(flow);
	}

}
