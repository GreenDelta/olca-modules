package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import com.google.common.base.Optional;

public class FlowDao extends BaseDao<Flow> implements IRootEntityDao<Flow> {

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

	public List<FlowDescriptor> getDescriptors(Optional<Category> category) {
		String jpql = "select f.id, f.name, f.description, loc.code from Flow f "
				+ "left join f.location loc ";
		Map<String, Category> params = null;
		if (category.isPresent()) {
			jpql += "where f.category = :category";
			params = Collections.singletonMap("category", category.get());
		} else {
			jpql += "where f.category is null";
			params = Collections.emptyMap();
		}
		return runDescriptorQuery(jpql, params);
	}

	private List<FlowDescriptor> runDescriptorQuery(String jpql,
			Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql, params);
			return toDescriptors(results);
		} catch (Exception e) {
			log.error("failed to get flow descriptors for " + jpql, e);
			return Collections.emptyList();
		}
	}

	private List<FlowDescriptor> toDescriptors(List<Object[]> results) {
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
		params.put("flowId", flow.getRefId());
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
