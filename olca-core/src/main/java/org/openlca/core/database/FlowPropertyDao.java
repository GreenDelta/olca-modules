package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;

import com.google.common.base.Optional;

public class FlowPropertyDao extends BaseDao<FlowProperty> implements
		IRootEntityDao<FlowProperty> {

	public FlowPropertyDao(EntityManagerFactory emf) {
		super(FlowProperty.class, emf);
	}

	public List<FlowPropertyDescriptor> getDescriptors() throws Exception {
		String jpql = "select p.id, p.name, p.description from FlowProperty p";
		List<Object[]> results = Query.on(getEntityFactory()).getAll(
				Object[].class, jpql);
		return createDescriptors(results);
	}

	public List<FlowPropertyDescriptor> getDescriptors(
			Optional<Category> category) {
		String jpql = "select p.id, p.name, p.description from FlowProperty p ";
		Map<String, Category> params = null;
		if (category.isPresent()) {
			jpql += "where p.category = :category";
			params = Collections.singletonMap("category", category.get());
		} else {
			jpql += "where p.category is null";
			params = Collections.emptyMap();
		}
		return runDescriptorQuery(jpql, params);
	}

	private List<FlowPropertyDescriptor> runDescriptorQuery(String jpql,
			Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql, params);
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("failed to get flow properties for category", e);
			return Collections.emptyList();
		}
	}

	private List<FlowPropertyDescriptor> createDescriptors(List<Object[]> vals) {
		List<FlowPropertyDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : vals) {
			FlowPropertyDescriptor descriptor = new FlowPropertyDescriptor();
			descriptor.setId((String) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

	public List<BaseDescriptor> whereUsed(FlowProperty prop) {
		return new FlowPropertyUseSearch(getEntityFactory()).findUses(prop);
	}

}
