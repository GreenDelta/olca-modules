package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

public class UnitGroupDao extends CategorizedEnitityDao<UnitGroup> {

	public UnitGroupDao(EntityManagerFactory emf) {
		super(UnitGroup.class, emf);
	}

	public List<BaseDescriptor> whereUsed(UnitGroup group) {
		if (group == null || group.getRefId() == null)
			return Collections.emptyList();
		String jpql = "select fp.id, fp.name, fp.description from FlowProperty fp "
				+ "where fp.unitGroupId = :unitGroupId";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("unitGroupId", group.getRefId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				FlowPropertyDescriptor d = new FlowPropertyDescriptor();
				d.setId((Long) result[0]);
				d.setName((String) result[1]);
				d.setDescription((String) result[2]);
				descriptors.add(d);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to search for unit group usages", e);
			return Collections.emptyList();
		}
	}

}
