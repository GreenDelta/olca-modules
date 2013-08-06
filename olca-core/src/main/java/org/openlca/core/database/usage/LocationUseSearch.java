package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationUseSearch implements IUseSearch<BaseDescriptor> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	LocationUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<BaseDescriptor> findUses(BaseDescriptor location) {
		if (location == null)
			return Collections.emptyList();
		List<BaseDescriptor> locations = findInFlows(location);
		List<BaseDescriptor> processes = findInProcesses(location);
		List<BaseDescriptor> descriptors = new ArrayList<>(locations.size()
				+ processes.size() + 2);
		descriptors.addAll(locations);
		descriptors.addAll(processes);
		return descriptors;
	}

	private List<BaseDescriptor> findInFlows(BaseDescriptor location) {
		String jpql = "select f.id, f.name, f.description, f.flowType, f.location.id, f.category.id from Flow f"
				+ " where f.location.id = :locationId";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql,
					Collections.singletonMap("locationId", location.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				FlowDescriptor d = new FlowDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				d.setFlowType((FlowType) result[3]);
				d.setLocation((Long) result[4]);
				d.setCategory((Long) result[5]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search flows with used location", e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> findInProcesses(BaseDescriptor location) {
		String jpql = "select p.id, p.name, p.description, p.processType, p.infrastructureProcess, p.location.id, p.category.id "
				+ " from Process p where p.location.id = :locationId";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql,
					Collections.singletonMap("locationId", location.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ProcessDescriptor d = new ProcessDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				d.setProcessType((ProcessType) result[3]);
				d.setInfrastructureProcess((Boolean) result[4]);
				d.setLocation((Long) result[5]);
				d.setCategory((Long) result[6]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search processes with used location", e);
			return Collections.emptyList();
		}
	}
}
