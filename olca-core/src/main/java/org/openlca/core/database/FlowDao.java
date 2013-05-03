package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class FlowDao extends BaseDao<Flow> {

	public FlowDao(EntityManagerFactory factory) {
		super(Flow.class, factory);
	}

	public FlowDescriptor getDescriptor(String id) {
		String jpql = "select f.name, f.description, loc.code from Flow f "
				+ "left join f.location loc where f.id = :id";
		try {
			Object[] result = Query.on(getEntityFactory()).getFirst(
					Object[].class, jpql, Collections.singletonMap("id", id));
			if (result == null)
				return null;
			FlowDescriptor descriptor = new FlowDescriptor();
			descriptor.setId(id);
			descriptor.setName((String) result[0]);
			descriptor.setDescription((String) result[1]);
			descriptor.setLocationCode((String) result[2]);
			return descriptor;
		} catch (Exception e) {
			log.error("Failed to load flow descriptor " + id);
			return null;
		}
	}

	public List<FlowDescriptor> getDescriptors(Category category)
			throws Exception {
		String categoryId = category == null ? null : category.getId();
		String jpql = "select f.id, f.name, f.description, loc.code from Flow f "
				+ "left join f.location loc "
				+ "where f.categoryId = :categoryId";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql,
				Collections.singletonMap("categoryId", categoryId));
		List<FlowDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			FlowDescriptor descriptor = new FlowDescriptor();
			descriptor.setId((String) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptor.setLocationCode((String) result[3]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

	public String getRefUnitName(Flow flow) throws Exception {
		FlowProperty prop = flow.getReferenceFlowProperty();
		if (prop == null || prop.getUnitGroupId() == null)
			return "";
		BaseDao<UnitGroup> unitDao = new BaseDao<>(UnitGroup.class,
				getEntityFactory());
		UnitGroup unitGroup = unitDao.getForId(prop.getUnitGroupId());
		if (unitGroup == null || unitGroup.getReferenceUnit() == null)
			return "";
		return unitGroup.getReferenceUnit().getName();
	}

	public List<ProcessDescriptor> getProviders(Flow flow) throws Exception {
		List<String> processIds = getProcessIdsWhereUsed(flow, false);
		return loadProcessDescriptors(processIds);
	}

	public List<ProcessDescriptor> getRecipients(Flow flow) throws Exception {
		List<String> processIds = getProcessIdsWhereUsed(flow, true);
		return loadProcessDescriptors(processIds);
	}

	private List<String> getProcessIdsWhereUsed(Flow flow, boolean input)
			throws Exception {
		if (flow == null)
			return Collections.emptyList();
		String jpql = "select p.id from Process p join p.exchanges e "
				+ "where e.flow.id = :flowId and e.input = :input ";
		Map<String, Object> params = new HashMap<>();
		params.put("flowId", flow.getId());
		params.put("input", input);
		return query().getAll(String.class, jpql, params);
	}

	private List<ProcessDescriptor> loadProcessDescriptors(
			List<String> processIds) throws Exception {
		if (processIds == null || processIds.isEmpty())
			return Collections.emptyList();
		// TODO: performance may could be improved if we query the
		// database with an 'IN - query'
		List<ProcessDescriptor> results = new ArrayList<>();
		ProcessDao dao = new ProcessDao(getEntityFactory());
		for (String processId : processIds) {
			ProcessDescriptor d = dao.getDescriptor(processId);
			if (d != null)
				results.add(d);
		}
		return results;
	}

	public List<BaseDescriptor> whereUsed(Flow flow) {
		return new FlowUseSearch(getEntityFactory()).findUses(flow);
	}

}
