package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class RootEntityDao<T extends RootEntity, V extends BaseDescriptor>
		extends BaseDao<T> {

	private Class<V> descriptorType;

	public RootEntityDao(Class<T> entityType, Class<V> descriptorType,
			IDatabase database) {
		super(entityType, database);
		this.descriptorType = descriptorType;
	}

	public V getDescriptor(long id) {
		String jpql = getDescriptorQuery() + " where e.id = :id";
		try {
			Object[] result = Query.on(getDatabase()).getFirst(Object[].class,
					jpql, Collections.singletonMap("id", id));
			return createDescriptor(result);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to get descriptor for "
					+ id, e);
			return null;
		}
	}

	public List<V> getDescriptors(List<Long> ids) {
		String jpql = getDescriptorQuery() + " where e.id in :ids";
		try {
			List<Object[]> results = Query.on(getDatabase()).getAll(
					Object[].class, jpql, Collections.singletonMap("ids", ids));
			return createDescriptors(results);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to get descriptor for "
					+ ids, e);
			return null;
		}
	}

	/**
	 * Returns all descriptors of the entity type of this DAO from the database.
	 */
	public List<V> getDescriptors() {
		try {
			String jpql = getDescriptorQuery();
			List<Object[]> results = Query.on(getDatabase()).getAll(
					Object[].class, jpql);
			return createDescriptors(results);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"failed to query all descriptors for " + entityType, e);
			return Collections.emptyList();
		}
	}

	/**
	 * Returns the default descriptor query. The query can be overwritten by
	 * subclasses. But as this query is used in other queries it must be assured
	 * that the name of the respective entity type in the query is 'e'.
	 */
	protected String getDescriptorQuery() {
		return "select e.id, e.name, e.description from "
				+ entityType.getSimpleName() + " e ";
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
			descriptor.setName((String) queryResult[1]);
			descriptor.setDescription((String) queryResult[2]);
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
