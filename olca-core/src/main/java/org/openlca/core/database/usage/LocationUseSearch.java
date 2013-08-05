package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationUseSearch implements IUseSearch<Location> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	LocationUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<BaseDescriptor> findUses(Location location) {
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

	private List<BaseDescriptor> findInFlows(Location location) {
		String jpql = "select distinct f.id, f.name, f.description, f.flowType from Flow f"
				+ " where f.location = :location";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql, Collections.singletonMap("location", location));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				FlowDescriptor d = new FlowDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				d.setFlowType((FlowType) result[3]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search flows with used location", e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> findInProcesses(Location location) {
		String jpql = "select distinct p.id, p.name, p.description, loc.code from Process p"
				+ " left join p.location loc"
				+ " where p.location = :location";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql, Collections.singletonMap("location", location));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ProcessDescriptor d = new ProcessDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				d.setLocationCode((String) result[3]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search processes with used location", e);
			return Collections.emptyList();
		}
	}
}
