package org.openlca.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Table;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.util.Strings;

public class RootEntityDao<T extends RootEntity, V extends BaseDescriptor> extends BaseDao<T> {

	private Class<V> descriptorType;
	private String entityTable;

	public RootEntityDao(Class<T> entityType, Class<V> descriptorType, IDatabase database) {
		super(entityType, database);
		this.descriptorType = descriptorType;
	}

	Class<V> getDescriptorType() {
		return descriptorType;
	}

	public V getDescriptor(long id) {
		String sql = getDescriptorQuery() + " where id = ?";
		Object[] result = selectFirst(sql, getDescriptorFields(), Collections.singletonList((Object) id));
		return createDescriptor(result);
	}

	public List<V> getDescriptors(Set<Long> ids) {
		if (ids == null || ids.isEmpty())
			return Collections.emptyList();
		if (ids.size() > MAX_LIST_SIZE)
			return executeChunked(ids, this::getDescriptors);
		String sql = getDescriptorQuery() + " where id in (" + Strings.join(ids, ',') + ")";
		List<Object[]> results = selectAll(sql, getDescriptorFields(), Collections.emptyList());
		return createDescriptors(results);
	}

	public List<V> getDescriptorsForRefIds(Set<String> refIds) {
		if (refIds == null || refIds.isEmpty())
			return Collections.emptyList();
		if (refIds.size() > MAX_LIST_SIZE)
			return executeChunked(refIds, this::getDescriptorsForRefIds);
		Set<String> quotedIds = new HashSet<>();
		for (String refId : refIds) {
			quotedIds.add('\'' + refId + '\'');
		}
		String sql = getDescriptorQuery() + " where ref_id in (" + Strings.join(quotedIds, ',') + ")";
		List<Object[]> results = selectAll(sql, getDescriptorFields(), Collections.emptyList());
		return createDescriptors(results);
	}

	/**
	 * Returns all descriptors of the entity type of this DAO from the database.
	 */
	public List<V> getDescriptors() {
		String sql = getDescriptorQuery();
		List<Object[]> results = selectAll(sql, getDescriptorFields(), Collections.emptyList());
		return createDescriptors(results);
	}

	protected final String getDescriptorQuery() {
		return "select " + Strings.join(getDescriptorFields(), ',') + " from " + getEntityTable();
	}

	private String getEntityTable() {
		if (entityTable == null)
			entityTable = entityType.getAnnotation(Table.class).name();
		return entityTable;
	}

	/**
	 * Returns all fields that should be queried by the descriptor query.
	 * Subclass may override to provide more information. Use sql column names !
	 */
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "version", "last_change" };
	}

	/**
	 * Creates a list of descriptors from a list of query results.
	 */
	protected List<V> createDescriptors(List<Object[]> results) {
		if (results == null)
			return Collections.emptyList();
		List<V> descriptors = new ArrayList<>(results.size());
		for (Object[] result : results) {
			V descriptor = createDescriptor(result);
			if (descriptor != null)
				descriptors.add(descriptor);
		}
		return descriptors;
	}

	/**
	 * Creates a descriptor from the given result of a descriptor query. This
	 * method can be overwritten by subclasses but it must be implemented in a
	 * way that it matches the respective descriptor query.
	 */
	protected V createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		V descriptor = null;
		try {
			descriptor = descriptorType.newInstance();
			descriptor.setId((Long) queryResult[0]);
			descriptor.setRefId((String) queryResult[1]);
			descriptor.setName((String) queryResult[2]);
			descriptor.setDescription((String) queryResult[3]);
			if (queryResult[4] != null)
				descriptor.setVersion((long) queryResult[4]);
			if (queryResult[5] != null)
				descriptor.setLastChange((long) queryResult[5]);
			descriptor.setType(ModelType.forModelClass(entityType));
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to map query result to descriptor", e);
		}
		return descriptor;
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
			PreparedStatement stmt = con.prepareStatement(query);
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
