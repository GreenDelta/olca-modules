package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class RootEntityDao<T extends RootEntity> extends BaseDao<T> {

	public RootEntityDao(Class<T> clazz, EntityManagerFactory factory) {
		super(clazz, factory);
	}

	/**
	 * Returns all descriptors of the entity type of this DAO from the database.
	 */
	public List<BaseDescriptor> getDescriptors() {
		try {
			String jpql = getDescriptorQuery();
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql);
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("failed to query all descriptors for " + entityType, e);
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
	protected List<BaseDescriptor> createDescriptors(List<Object[]> results) {
		if (results == null)
			return Collections.emptyList();
		List<BaseDescriptor> descriptors = new ArrayList<>(results.size());
		for (Object[] result : results) {
			BaseDescriptor descriptor = createDescriptor(result);
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
	protected BaseDescriptor createDescriptor(Object[] queryResult) {
		BaseDescriptor descriptor = new BaseDescriptor();
		try {
			descriptor.setId((Long) queryResult[0]);
			descriptor.setName((String) queryResult[1]);
			descriptor.setDescription((String) queryResult[2]);
			descriptor.setType(ModelType.forModelClass(entityType));
		} catch (Exception e) {
			log.error("failed to map query result to descriptor", e);
		}
		return descriptor;
	}

	public T getForRefId(String refId) {
		if (refId == null)
			return null;
		String jpql = "select e from " + entityType.getSimpleName()
				+ " e where e.refId = :refId";
		try {
			return Query.on(getEntityFactory()).getFirst(entityType, jpql,
					Collections.singletonMap("refId", refId));
		} catch (Exception e) {
			log.error("failed to get instance for refId " + refId, e);
			return null;
		}
	}

}
