package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.common.base.Optional;

public class CategorizedEnitityDao<T extends CategorizedEntity> extends
		RootEntityDao<T> {

	public CategorizedEnitityDao(Class<T> clazz, EntityManagerFactory factory) {
		super(clazz, factory);
	}

	public List<BaseDescriptor> getDescriptors(Optional<Category> category) {
		String jpql = getDescriptorQuery();
		Map<String, Category> params = null;
		if (category.isPresent()) {
			jpql += "where e.category = :category";
			params = Collections.singletonMap("category", category.get());
		} else {
			jpql += "where e.category is null";
			params = Collections.emptyMap();
		}
		return runDescriptorQuery(jpql, params);
	}

	private List<BaseDescriptor> runDescriptorQuery(String jpql,
			Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql, params);
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("Failed to get descriptors for category " + params, e);
			return Collections.emptyList();
		}
	}

}
