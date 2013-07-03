package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowPropertyUseSearch implements IUseSearch<FlowProperty> {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EntityManagerFactory emf;

	public FlowPropertyUseSearch(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@Override
	public List<BaseDescriptor> findUses(FlowProperty prop) {
		if (prop == null || prop.getRefId() == null)
			return Collections.emptyList();
		List<BaseDescriptor> flows = findInFlows(prop);
		List<BaseDescriptor> unitGroups = findInUnitGroups(prop);
		List<BaseDescriptor> results = new ArrayList<>(flows.size()
				+ unitGroups.size() + 2);
		results.addAll(flows);
		results.addAll(unitGroups);
		return results;
	}

	private List<BaseDescriptor> findInFlows(FlowProperty prop) {
		String jpql = "select f.id, f.name, f.description from Flow f "
				+ "join f.flowPropertyFactors fp where fp.flowProperty.id = :propId";
		try {
			List<Object[]> results = Query.on(emf).getAll(Object[].class, jpql,
					Collections.singletonMap("propId", prop.getRefId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				FlowDescriptor d = new FlowDescriptor();
				d.setId((String) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search flow properties in flows", e);
			return Collections.emptyList();
		}
	}

	private List<BaseDescriptor> findInUnitGroups(FlowProperty prop) {
		String jpql = "select ug.id, ug.name, ug.description from UnitGroup ug "
				+ "where ug.defaultFlowProperty.id = :propId";
		try {
			List<Object[]> results = Query.on(emf).getAll(Object[].class, jpql,
					Collections.singletonMap("propId", prop.getRefId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				UnitGroupDescriptor d = new UnitGroupDescriptor();
				d.setId((String) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search flow properties in flows", e);
			return Collections.emptyList();
		}
	}

}
