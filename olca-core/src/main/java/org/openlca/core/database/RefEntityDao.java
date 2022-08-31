package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.persistence.Table;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Strings;

import gnu.trove.map.hash.TLongObjectHashMap;

public class RefEntityDao<T extends RefEntity, V extends Descriptor> extends BaseDao<T> {

	private final Class<V> descriptorType;
	private String entityTable;

	protected RefEntityDao(Class<T> entityType, Class<V> descriptorType, IDatabase db) {
		super(entityType, db);
		this.descriptorType = descriptorType;
	}

	@Override
	public T insert(T entity) {
		entity = super.insert(entity);
		db.notifyInsert(Descriptor.of(entity));
		return entity;
	}

	@Override
	public T update(T entity) {
		entity = super.update(entity);
		db.notifyUpdate(Descriptor.of(entity));
		return entity;
	}

	@Override
	public void delete(T entity) {
		super.delete(entity);
		db.notifyDelete(Descriptor.of(entity));
	}

	@Override
	public void deleteAll(Collection<T> entities) {
		super.deleteAll(entities);
		for (T entity : entities) {
			db.notifyDelete(Descriptor.of(entity));
		}
	}

	Class<V> getDescriptorType() {
		return descriptorType;
	}

	public V getDescriptor(long id) {
		var list = queryDescriptors("where d.id = ?", id);
		return list.isEmpty() ? null : list.get(0);
	}

	public List<V> getDescriptors(Set<Long> ids) {
		if (ids == null || ids.isEmpty())
			return Collections.emptyList();
		if (ids.size() > MAX_LIST_SIZE)
			return executeChunked(ids, this::getDescriptors);
		var list = "(" + Strings.join(ids, ',') + ")";
		return queryDescriptors("where d.id in " + list, List.of());
	}

	public V getDescriptorForRefId(String refId) {
		var list = queryDescriptors("where d.ref_id = ?", refId);
		return list.isEmpty()
				? null
				: list.get(0);
	}

	/**
	 * Returns all descriptors of the entity type of this DAO from the database.
	 */
	public List<V> getDescriptors() {
		return queryDescriptors();
	}

	protected final List<V> queryDescriptors() {
		return queryDescriptors(null, List.of());
	}

	protected final List<V> queryDescriptors(String condition, Object param) {
		var params = param == null
				? Collections.emptyList()
				: List.of(param);
		return queryDescriptors(condition, params);
	}

	protected List<V> queryDescriptors(String condition, List<Object> params) {
		var sql = """
						select
							d.id,
							d.ref_id,
							d.name,
							d.description from
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
			d.description = r.getString(4);
			list.add(d);
			return true;
		});
		return list;
	}

	protected Supplier<V> descriptorConstructor() {
		try {
			var cons = descriptorType.getDeclaredConstructor();
			return () -> {
				try {
					return cons.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("failed create constructor instance", e);
				}
			};
		} catch (Exception e) {
			throw new RuntimeException("failed create constructor instance", e);
		}
	}

	/**
	 * Returns all descriptors of the DAO type in a map which is indexed by the IDs
	 * of the respective descriptors.
	 */
	public TLongObjectHashMap<V> descriptorMap() {
		var descriptors = getDescriptors();
		var map = new TLongObjectHashMap<V>(descriptors.size());
		for (var d : descriptors) {
			map.put(d.id, d);
		}
		return map;
	}

	String getEntityTable() {
		if (entityTable == null) {
			entityTable = entityType.getAnnotation(Table.class).name();
		}
		return entityTable;
	}

	public T getForRefId(String refId) {
		if (refId == null)
			return null;
		String jpql = "select e from " + entityType.getSimpleName() + " e where e.refId = :refId";
		try {
			return Query.on(getDatabase()).getFirst(entityType, jpql, Collections.singletonMap("refId", refId));
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to get instance for refId " + refId, e);
			return null;
		}
	}

	public List<T> getForRefIds(Set<String> refIds) {
		if (refIds == null || refIds.isEmpty())
			return Collections.emptyList();
		if (refIds.size() > MAX_LIST_SIZE)
			return executeChunked(refIds, this::getForRefIds);
		String jpql = "select e from " + entityType.getSimpleName() + " e where e.refId in :refIds";
		try {
			return Query.on(getDatabase()).getAll(entityType, jpql, Collections.singletonMap("refIds", refIds));
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to get instance for refId list", e);
			return null;
		}
	}

	/**
	 * Returns true if an entity with the given reference ID is in the database.
	 */
	public boolean contains(String refId) {
		try (Connection con = getDatabase().createConnection()) {
			String query = "select count(*) from " + getEntityTable() + " where ref_id = ?";
			var stmt = con.prepareStatement(query);
			stmt.setString(1, refId);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			boolean b = rs.getLong(1) > 0;
			rs.close();
			stmt.close();
			return b;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "contains query failed for refId=" + refId, e);
			return false;
		}
	}

	public List<T> getForName(String name) {
		try {
			return Query.on(getDatabase()).getAllForName(entityType, name);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to get instance for name " + name, e);
			return null;
		}
	}
}
