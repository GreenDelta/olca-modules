package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Flow;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowUseSearch implements IUseSearch<Flow> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EntityManagerFactory emf;

	public FlowUseSearch(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@Override
	public List<BaseDescriptor> findUses(Flow flow) {
		if (flow == null || flow.getRefId() == null)
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
		String jpql = "select distinct m.id, m.name, m.description from LCIAMethod m"
				+ " join m.lciaCategories cat join cat.lciaFactors fac "
				+ " where fac.flow.id = :flowId";
		try {
			List<Object[]> results = Query.on(emf).getAll(Object[].class, jpql,
					Collections.singletonMap("flowId", flow.getRefId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ImpactMethodDescriptor d = new ImpactMethodDescriptor();
				d.setId((String) result[0]);
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
				+ "loc where e.flow.id = :flowId";
		try {
			List<Object[]> results = Query.on(emf).getAll(Object[].class, jpql,
					Collections.singletonMap("flowId", flow.getRefId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				ProcessDescriptor d = new ProcessDescriptor();
				d.setId((String) result[0]);
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
