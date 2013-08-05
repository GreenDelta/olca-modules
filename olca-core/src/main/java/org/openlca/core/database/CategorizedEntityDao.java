package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.common.base.Optional;

public class CategorizedEntityDao<T extends CategorizedEntity, V extends BaseDescriptor>
		extends RootEntityDao<T, V> {

	public CategorizedEntityDao(Class<T> entityType, Class<V> descriptorType,
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
		EntityManager em = createManager();
		TypedQuery<?> query = em.createQuery(jpql, entityType);
		query.setParameter("id", model.getId());
		query.setParameter("category", category.isPresent() ? category.get()
				: null);
		try {
			em.getTransaction().begin();
			query.executeUpdate();
			em.getTransaction().commit();
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to update category for "
					+ entityType.getSimpleName(), e);
		} finally {
			em.close();
		}
	}

	private List<V> runDescriptorQuery(String jpql, Map<String, Category> params) {
		try {
			List<Object[]> results = Query.on(getDatabase()).getAll(
					Object[].class, jpql, params);
			return createDescriptors(results);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"Failed to get descriptors for category " + params, e);
			return Collections.emptyList();
		}
	}

}
