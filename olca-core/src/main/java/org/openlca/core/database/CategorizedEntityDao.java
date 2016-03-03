package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

import com.google.common.base.Optional;

public class CategorizedEntityDao<T extends CategorizedEntity, V extends CategorizedDescriptor>
		extends RootEntityDao<T, V> {

	public CategorizedEntityDao(Class<T> entityType, Class<V> descriptorType,
			IDatabase database) {
		super(entityType, descriptorType, database);
	}

	public List<V> getDescriptors(Optional<Category> category) {
		String sql = getDescriptorQuery();
		if (category.isPresent()) {
			sql += " where f_category = ?";
			List<Object[]> results = selectAll(sql, getDescriptorFields(),
					Collections.singletonList((Object) category.get().getId()));
			return createDescriptors(results);
		} else {
			sql += " where f_category is null";
			List<Object[]> results = selectAll(sql, getDescriptorFields(),
					Collections.emptyList());
			return createDescriptors(results);
		}
	}

	@Override
	protected V createDescriptor(Object[] queryResult) {
		V descriptor = super.createDescriptor(queryResult);
		if (descriptor != null)
			descriptor.setCategory((Long) queryResult[6]);
		return descriptor;
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "version",
				"last_change", "f_category" };
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

}
