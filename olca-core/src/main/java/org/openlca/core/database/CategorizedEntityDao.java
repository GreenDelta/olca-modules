package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

public class CategorizedEntityDao<T extends CategorizedEntity, V extends CategorizedDescriptor>
		extends RootEntityDao<T, V> {

	protected CategorizedEntityDao(Class<T> entityType, Class<V> descriptorType,
								   IDatabase database) {
		super(entityType, descriptorType, database);
	}

	public List<V> getDescriptors(Optional<Category> category) {
		String sql = getDescriptorQuery();
		if (category.isPresent()) {
			sql += " where f_category = ?";
			List<Object[]> results = selectAll(sql, getDescriptorFields(),
					Collections.singletonList(category.get().id));
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
		var d = super.createDescriptor(queryResult);
		if (d == null)
			return d;
		if (queryResult[6] != null) {
			d.category = (Long) queryResult[6];
		}
		if (queryResult[7] != null) {
			d.library = (String) queryResult[7];
		}
		return d;
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[]{
				"id",
				"ref_id",
				"name",
				"description",
				"version",
				"last_change",
				"f_category",
				"library"
		};
	}

	public CategorizedDescriptor updateCategory(CategorizedDescriptor model,
												Optional<Category> category) {
		Version v = new Version(model.version);
		v.incUpdate();
		long version = v.getValue();
		long lastChange = System.currentTimeMillis();
		model.version = version;
		model.lastChange = lastChange;
		model.category = category.isPresent() ? category.get().id : null;
		String jpql = "update " + entityType.getSimpleName()
				+ " e set e.category = :category, e.version = :version, e.lastChange = :lastChange where e.id = :id";
		EntityManager em = createManager();
		TypedQuery<?> query = em.createQuery(jpql, entityType);
		query.setParameter("id", model.id);
		query.setParameter("category",
				category.isPresent() ? category.get() : null);
		query.setParameter("version", version);
		query.setParameter("lastChange", lastChange);
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
		database.notifyUpdate(model);
		return model;
	}

}
