package org.openlca.core.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.RootDescriptor;

import jakarta.persistence.TypedQuery;

public class RootEntityDao<T extends RootEntity, V extends RootDescriptor>
		extends RefEntityDao<T, V> {

	protected RootEntityDao(Class<T> entityType, Class<V> descriptorType,
			IDatabase database) {
		super(entityType, descriptorType, database);
	}

	public List<V> getDescriptors(Optional<Category> category) {
		if (category.isPresent()) {
			return queryDescriptors("where d.f_category = ?", category.get().id);
		} else {
			return queryDescriptors("where d.f_category is null", List.of());
		}
	}

	@Override
	protected List<V> queryDescriptors(String condition, List<Object> params) {
		var sql = """
					select
						d.id,
				   	d.ref_id,
				   	d.name,
				   	d.version,
				   	d.last_change,
				   	d.f_category,
				   	d.library,
				   	d.tags from
				""" + getEntityTable() + " d";
		if (condition != null) {
			sql += " " + condition;
		}

		var cons = descriptorConstructor();
		var list = new ArrayList<V>();
		NativeSql.on(db).query(sql, params, r -> {
			var d = cons.get();
			d.id = r.getLong(1);
			d.refId = r.getString(2);
			d.name = r.getString(3);
			d.version = r.getLong(4);
			d.lastChange = r.getLong(5);
			var catId = r.getLong(6);
			if (!r.wasNull()) {
				d.category = catId;
			}
			d.library = r.getString(7);
			d.tags = r.getString(8);
			list.add(d);
			return true;
		});
		return list;
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
		query.setParameter("category", category.orElse(null));
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
		return model;
	}

}
