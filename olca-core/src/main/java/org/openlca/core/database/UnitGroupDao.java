package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupDao extends BaseDao<UnitGroup> implements
		IRootEntityDao<UnitGroup> {

	public UnitGroupDao(EntityManagerFactory emf) {
		super(UnitGroup.class, emf);
	}

	public List<UnitGroupDescriptor> getDescriptors() throws Exception {
		String jpql = "select u.id, u.name, u.description from UnitGroup u";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql);
		return createDescriptors(results);
	}

	private List<UnitGroupDescriptor> createDescriptors(List<Object[]> vals) {
		List<UnitGroupDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : vals) {
			UnitGroupDescriptor descriptor = new UnitGroupDescriptor();
			descriptor.setId((String) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

	public List<UnitGroupDescriptor> getDescriptors(Category category) {
		log.trace("get unit groups for category {}", category);
		String jpql = "select u.id, u.name, u.description from UnitGroup u "
				+ "where u.category = :category";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("category", category));
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("failed to get unit groups for category " + category, e);
			return Collections.emptyList();
		}
	}

	public List<BaseDescriptor> whereUsed(UnitGroup group) {
		if (group == null || group.getId() == null)
			return Collections.emptyList();
		String jpql = "select fp.id, fp.name, fp.description from FlowProperty fp "
				+ "where fp.unitGroupId = :unitGroupId";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("unitGroupId", group.getId()));
			List<BaseDescriptor> descriptors = new ArrayList<>();
			for (Object[] result : results) {
				FlowPropertyDescriptor d = new FlowPropertyDescriptor();
				d.setId((String) result[0]);
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
