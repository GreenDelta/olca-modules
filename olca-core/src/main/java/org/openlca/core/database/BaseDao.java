package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDao<T> {

	/**
	 * A database dependent field for the maximum size of lists in JPQL queries.
	 */
	static final int MAX_LIST_SIZE = 1000;

	protected Class<T> entityType;
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private EntityManagerFactory entityFactory;

	public BaseDao(Class<T> entityType, EntityManagerFactory factory) {
		this.entityType = entityType;
		this.entityFactory = factory;
	}

	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	public boolean contains(String id) throws Exception {
		if (id == null)
			return false;
		return getForId(id) != null;
	}

	public void delete(T entity) throws Exception {
		if (entity == null)
			return;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			em.remove(em.merge(entity));
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public void deleteAll(Collection<T> entities) throws Exception {
		if (entities == null)
			return;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			for (T entity : entities) {
				log.trace("About to remove entity {}", entity);
				em.remove(em.merge(entity));
			}
			em.getTransaction().commit();
		} finally {
			em.close();
		}
	}

	public T update(T entity) throws Exception {
		if (entity == null)
			return null;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			T retval = em.merge(entity);
			em.getTransaction().commit();
			return retval;
		} finally {
			em.close();
		}
	}

	public T insert(T entity) throws Exception {
		if (entity == null)
			return null;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			em.persist(entity);
			em.getTransaction().commit();
			return entity;
		} finally {
			em.close();
		}
	}

	public T getForId(String id) throws Exception {
		if (id == null)
			return null;
		EntityManager entityManager = createManager();
		try {
			T o = entityManager.find(entityType, id);
			return o;
		} finally {
			entityManager.close();
		}
	}

	public List<T> getForIds(Set<String> ids) throws Exception {
		if (ids == null || ids.isEmpty())
			return Collections.emptyList();
		if (ids.size() <= MAX_LIST_SIZE)
			return fetchForIds(ids);
		return fetchForChunkedIds(ids);
	}

	private List<T> fetchForChunkedIds(Set<String> ids) {
		List<String> rest = new ArrayList<>(ids);
		List<T> results = new ArrayList<>();
		while (!rest.isEmpty()) {
			int toPos = rest.size() > MAX_LIST_SIZE ? MAX_LIST_SIZE : rest
					.size();
			List<String> nextChunk = rest.subList(0, toPos);
			List<T> chunkResults = fetchForIds(nextChunk);
			results.addAll(chunkResults);
			nextChunk.clear(); // clears also the elements in rest
		}
		return results;
	}

	private List<T> fetchForIds(Collection<String> ids) {
		EntityManager em = createManager();
		try {
			String jpql = "SELECT o FROM " + entityType.getSimpleName()
					+ " o WHERE o.id IN :ids";
			TypedQuery<T> query = em.createQuery(jpql, entityType);
			query.setParameter("ids", ids);
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	public List<T> getAll() throws Exception {
		log.debug("Select all for class {}", entityType);
		EntityManager em = createManager();
		try {
			String jpql = "SELECT o FROM ".concat(entityType.getSimpleName())
					.concat(" o");
			TypedQuery<T> query = em.createQuery(jpql, entityType);
			List<T> results = query.getResultList();
			log.debug("{} results", results.size());
			return results;
		} finally {
			em.close();
		}
	}

	public List<T> getAll(String jpql, Map<String, ? extends Object> parameters)
			throws Exception {
		EntityManager em = createManager();
		try {
			TypedQuery<T> query = em.createQuery(jpql, entityType);
			for (String param : parameters.keySet()) {
				query.setParameter(param, parameters.get(param));
			}
			List<T> results = query.getResultList();
			return results;
		} finally {
			em.close();
		}
	}

	public T getFirst(String jpql, Map<String, ? extends Object> parameters)
			throws Exception {
		List<T> list = getAll(jpql, parameters);
		if (list.isEmpty())
			return null;
		return list.get(0);
	}

	public long getCount(String jpql, Map<String, Object> parameters)
			throws Exception {
		EntityManager em = createManager();
		try {
			TypedQuery<Long> query = em.createQuery(jpql, Long.class);
			for (String param : parameters.keySet()) {
				query.setParameter(param, parameters.get(param));
			}
			Long count = query.getSingleResult();
			return count == null ? 0 : count;
		} finally {
			em.close();
		}
	}

	public void deleteAll() throws Exception {
		log.trace("delete all instances of {}", entityType);
		deleteAll(getAll());
	}

	protected EntityManager createManager() {
		EntityManager em = entityFactory.createEntityManager();
		return em;
	}

	protected Query query() {
		return Query.on(getEntityFactory());
	}

}
