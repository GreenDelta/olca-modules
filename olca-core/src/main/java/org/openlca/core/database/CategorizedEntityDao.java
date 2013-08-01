package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.common.base.Optional;

public class CategorizedEntityDao<T extends CategorizedEntity, V extends BaseDescriptor>
		extends RootEntityDao<T, V> {


	CategorizedEntityDao(Class<T> entityType, Class<V> descriptorType,
			IDatabase database) {
		super(entityType, descriptorType, database);
	}

	public List<V> getDescriptors(Optional<Category> category) {
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

	public void updateCategory(BaseDescriptor model, Optional<Category> category) {
		String jpql = "update " + entityType.getSimpleName()
				+ " e set e.category = :category where e.id = :id";
		TypedQuery<?> query = createManager().createQuery(jpql, entityType);
		query.setParameter("id", model.getId());
		query.setParameter("category", category.isPresent() ? category.get()
				: null);
		query.executeUpdate();
	}

	private List<V> runDescriptorQuery(String jpql, Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getDatabase()).getAll(
					Object[].class, jpql, params);
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("Failed to get descriptors for category " + params, e);
			return Collections.emptyList();
		}
	}

}
