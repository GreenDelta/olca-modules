package org.openlca.core.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for common queries.
 */
public class Query {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EntityManagerFactory entityFactory;

	private Query(EntityManagerFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	public static Query on(IDatabase database) {
		return new Query(database.getEntityFactory());
	}

	public static Query on(EntityManagerFactory entityFactory) {
		return new Query(entityFactory);
	}

	/**
	 * Searches an entity of the given type with the given name. The type must
	 * have a string field 'name'. If no such type is contained in the database,
	 * null is returned.
	 */
	public <T> T getForName(Class<T> type, String name) throws Exception {
		log.trace("query {} for name {}", type, name);
		BaseDao<T> dao = new BaseDao<>(type, entityFactory);
		String jpql = "select t from " + type.getSimpleName()
				+ " t where t.name = :name";
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		return dao.getFirst(jpql, map);
	}

	public <T> List<T> getAllForName(Class<T> type, String name)
			throws Exception {
		if (name == null)
			return null;
		String jpql = "select e from " + type.getSimpleName()
				+ " e where e.name = '" + name + "'";
		return getAll(type, jpql);
	}

	public <T> List<T> getAll(Class<T> type, String jpql) throws Exception {
		Map<String, Object> map = new HashMap<>();
		return getAll(type, jpql, map);
	}

	public <T> List<T> getAll(Class<T> type, String jpql,
			Map<String, ? extends Object> params) throws Exception {
		log.trace("Get all {} with query {}", type, jpql);
		EntityManager em = createManager();
		try {
			TypedQuery<T> query = em.createQuery(jpql, type);
			if (params != null)
				for (String key : params.keySet()) {
					query.setParameter(key, params.get(key));
				}
			List<T> results = query.getResultList();
			log.debug("{} results", results.size());
			return results;
		} finally {
			em.close();
		}
	}

	public <T> T getFirst(Class<T> type, String jpql,
			Map<String, ? extends Object> params) throws Exception {
		List<T> all = getAll(type, jpql, params);
		if (all == null || all.isEmpty())
			return null;
		return all.get(0);
	}

	private EntityManager createManager() {
		return entityFactory.createEntityManager();
	}

}
