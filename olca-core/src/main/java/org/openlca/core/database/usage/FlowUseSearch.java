package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.Flow;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowUseSearch implements IUseSearch<Flow> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	FlowUseSearch(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<BaseDescriptor> findUses(Flow flow) {
		if (flow == null)
			return Collections.emptyList();
		List<BaseDescriptor> processes = findInProcesses(flow);
		List<BaseDescriptor> methods = findInMethods(flow);
		List<BaseDescriptor> descriptors = new ArrayList<>(processes.size()
				+ methods.size() + 2);
		descriptors.addAll(processes);
		descriptors.addAll(methods);
		return descriptors;
	}

	private List<BaseDescriptor> findInMethods(Flow flow) {
		String jpql = "select m.id, m.name, m.description from ImpactMethod m"
				+ " join m.impactCategories cat join cat.impactFactors fac "
				+ " where fac.flow = :flow";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql, Collections.singletonMap("flow", flow));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ImpactMethodDescriptor d = new ImpactMethodDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search processes with used flow", e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> findInProcesses(Flow flow) {
		String jpql = "select p.id, p.name, p.description, loc.code "
				+ "from Process p join p.exchanges e left join p.location "
				+ "loc where e.flow = :flow";
		try {
			List<Object[]> results = Query.on(database).getAll(Object[].class,
					jpql, Collections.singletonMap("flow", flow));
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
			log.error("Failed to search processes with used flow", e);
			return Collections.emptyList();
		}
	}
}
