package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.Table;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.util.Strings;

public class RootEntityDao<T extends RootEntity, V extends BaseDescriptor>
		extends BaseDao<T> {

	private Class<V> descriptorType;
	private String entityTable;

	Class<V> getDescriptorType() {
		return descriptorType;
	}

	public RootEntityDao(Class<T> entityType, Class<V> descriptorType,
			IDatabase database) {
		super(entityType, database);
		this.descriptorType = descriptorType;
	}

	public V getDescriptor(long id) {
		String sql = getDescriptorQuery() + " where id = ?";
		Object[] result = selectFirst(sql, getDescriptorFields(),
				Collections.singletonList((Object) id));
		return createDescriptor(result);
	}

	public List<V> getDescriptors(Set<Long> ids) {
		String sql = getDescriptorQuery() + " where id in ("
				+ Strings.join(ids, ',') + ")";
		List<Object[]> results = selectAll(sql, getDescriptorFields(),
				Collections.emptyList());
		return createDescriptors(results);
	}

	/**
	 * Returns all descriptors of the entity type of this DAO from the database.
	 */
	public List<V> getDescriptors() {
		String sql = getDescriptorQuery();
		List<Object[]> results = selectAll(sql, getDescriptorFields(),
				Collections.emptyList());
		return createDescriptors(results);
	}

	protected final String getDescriptorQuery() {
		return "select " + Strings.join(getDescriptorFields(), ',') + " from "
				+ getEntityTable();
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
		return new String[] { "id", "ref_id", "name", "description" };
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
		V descriptor = null;
		try {
			descriptor = descriptorType.newInstance();
			descriptor.setId((Long) queryResult[0]);
			descriptor.setRefId((String) queryResult[1]);
			descriptor.setName((String) queryResult[2]);
			descriptor.setDescription((String) queryResult[3]);
			descriptor.setType(ModelType.forModelClass(entityType));
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"failed to map query result to descriptor", e);
		}
		return descriptor;
	}

	public T getForRefId(String refId) {
		if (refId == null)
			return null;
		String jpql = "select e from " + entityType.getSimpleName()
				+ " e where e.refId = :refId";
		try {
			return Query.on(getDatabase()).getFirst(entityType, jpql,
					Collections.singletonMap("refId", refId));
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"failed to get instance for refId " + refId, e);
			return null;
		}
	}

	public List<T> getForName(String name) {
		try {
			return Query.on(getDatabase()).getAllForName(entityType, name);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"failed to get instance for name " + name, e);
			return null;
		}
	}

}
