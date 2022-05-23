package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


import jakarta.persistence.TypedQuery;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.RootDescriptor;

public class RootEntityDao<T extends RootEntity, V extends RootDescriptor>
		extends RefEntityDao<T, V> {

	protected RootEntityDao(Class<T> entityType, Class<V> descriptorType,
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
	protected V createDescriptor(Object[] record) {
		var d = super.createDescriptor(record);
		if (d == null)
			return null;
		if (record[4] instanceof Number num) {
			d.version = num.longValue();
		}
		if (record[5] instanceof Number num) {
			d.lastChange = num.longValue();
		}
		if (record[6] != null) {
			d.category = (Long) record[6];
		}
		if (record[7] != null) {
			d.library = (String) record[7];
		}
		if (record[8] != null) {
			d.tags = (String) record[8];
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
				"library",
				"tags",
		};
	}

	public RootDescriptor updateCategory(RootDescriptor model,
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
		var em = db.newEntityManager();
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
		db.notifyUpdate(model);
		return model;
	}

}
