package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;
import org.openlca.core.model.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDao<T extends AbstractEntity> implements IDao<T> {

	/**
	 * A database dependent field for the maximum size of lists in JPQL queries.
	 */
	static final int MAX_LIST_SIZE = 1000;

	protected final Class<T> entityType;
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	protected final IDatabase db;

	protected BaseDao(Class<T> entityType, IDatabase db) {
		this.entityType = entityType;
		this.db = db;
	}

	public IDatabase getDatabase() {
		return db;
	}

	@Override
	public boolean contains(long id) {
		return getForId(id) != null;
	}

	@Override
	public Map<Long, Boolean> contains(Set<Long> ids) {
		if (ids == null || ids.isEmpty())
			return Collections.emptyMap();
		if (ids.size() > MAX_LIST_SIZE)
			return executeChunked2(ids, this::contains);
		Map<Long, Boolean> result = new HashMap<>();
		for (Long id : ids)
			result.put(id, false);
		var table = entityType.getDeclaredAnnotation(Table.class).name();
		var query = "SELECT id FROM " + table
				+ " WHERE id IN " + NativeSql.asList(ids);
		NativeSql.on(db).query(query, (entry) -> {
			long id = entry.getLong(1);
			result.put(id, true);
			return true;
		});
		return result;
	}

	@Override
	public void delete(T entity) {
		if (entity == null)
			return;
		var em = db.newEntityManager();
		try {
			em.getTransaction().begin();
			em.remove(em.merge(entity));
			em.getTransaction().commit();
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while deleting "
					+ entityType.getSimpleName(), e);
		} finally {
			em.close();
		}
	}

	@Override
	public void deleteAll(Collection<T> entities) {
		if (entities == null)
			return;
		var em = db.newEntityManager();
		try {
			em.getTransaction().begin();
			for (T entity : entities) {
				log.trace("About to remove entity {}", entity);
				em.remove(em.merge(entity));
			}
			em.getTransaction().commit();
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while deleting "
					+ entityType.getSimpleName(), e);
		} finally {
			em.close();
		}
	}

	@Override
	public T update(T entity) {
		if (entity == null)
			return null;
		var em = db.newEntityManager();
		try {
			em.getTransaction().begin();
			T retval = em.merge(entity);
			em.getTransaction().commit();
			return retval;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while updating "
					+ entityType.getSimpleName(), e);
			return entity;
		} finally {
			em.close();
		}
	}

	@Override
	public T insert(T entity) {
		if (entity == null)
			return null;
		var em = db.newEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(entity);
			em.getTransaction().commit();
			return entity;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while inserting "
					+ entityType.getSimpleName(), e);
			return entity;
		} finally {
			em.close();
		}
	}

	@Override
	public T getForId(long id) {
		log.trace("get {} for id={}", entityType, id);
		var entityManager = db.newEntityManager();
		try {
			return entityManager.find(entityType, id);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while loading "
					+ entityType.getSimpleName() + " with id " + id, e);
			return null;
		} finally {
			entityManager.close();
		}
	}

	@Override
	public List<T> getForIds(Set<Long> ids) {
		if (ids == null || ids.isEmpty())
			return Collections.emptyList();
		if (ids.size() > MAX_LIST_SIZE)
			return executeChunked(ids, this::getForIds);
		var em = db.newEntityManager();
		try {
			String jpql = "SELECT o FROM " + entityType.getSimpleName()
					+ " o WHERE o.id IN :ids";
			TypedQuery<T> query = em.createQuery(jpql, entityType);
			query.setParameter("ids", ids);
			return query.getResultList();
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while fetching for ids",
					e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}

	// Executes the query method chunked, (for methods with List return value)
	protected <X, Y> List<Y> executeChunked(Set<X> set,
			Function<Set<X>, List<Y>> queryMethod) {
		List<Set<X>> split = split(set);
		List<Y> all = new ArrayList<>();
		for (Set<X> s : split) {
			all.addAll(queryMethod.apply(s));
		}
		return all;
	}

	// Executes the query method chunked, (for methods with Map return value)
	protected <X, Y> Map<X, Y> executeChunked2(Set<X> set,
			Function<Set<X>, Map<X, Y>> queryMethod) {
		List<Set<X>> split = split(set);
		Map<X, Y> all = new HashMap<>();
		for (Set<X> s : split) {
			all.putAll(queryMethod.apply(s));
		}
		return all;
	}

	private <X> List<Set<X>> split(Set<X> all) {
		List<Set<X>> split = new ArrayList<>();
		List<X> rest = new ArrayList<>(all);
		while (!rest.isEmpty()) {
			int toPos = Math.min(rest.size(), MAX_LIST_SIZE);
			List<X> nextChunk = rest.subList(0, toPos);
			split.add(new HashSet<>(nextChunk));
			nextChunk.clear(); // clears also the elements in rest
		}
		return split;
	}

	@Override
	public List<T> getAll() {
		log.debug("Select all for class {}", entityType);
		var em = db.newEntityManager();
		try {
			String jpql = "SELECT o FROM ".concat(entityType.getSimpleName())
					.concat(" o");
			TypedQuery<T> query = em.createQuery(jpql, entityType);
			List<T> results = query.getResultList();
			log.debug("{} results", results.size());
			return results;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while loading all "
					+ entityType.getSimpleName(), e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}

	@Override
	public List<T> getAll(String jpql, Map<String, ?> parameters) {
		var em = db.newEntityManager();
		try {
			TypedQuery<T> query = em.createQuery(jpql, entityType);
			for (String param : parameters.keySet()) {
				query.setParameter(param, parameters.get(param));
			}
			return query.getResultList();
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while loading all "
					+ entityType.getSimpleName(), e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}

	@Override
	public T getFirst(String jpql, Map<String, ?> parameters) {
		List<T> list = getAll(jpql, parameters);
		if (list.isEmpty())
			return null;
		return list.get(0);
	}

	@Override
	public long getCount(String jpql, Map<String, Object> parameters) {
		var em = db.newEntityManager();
		try {
			TypedQuery<Long> query = em.createQuery(jpql, Long.class);
			for (String param : parameters.keySet()) {
				query.setParameter(param, parameters.get(param));
			}
			Long count = query.getSingleResult();
			return count == null ? 0 : count;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while getting count of "
					+ entityType.getSimpleName(), e);
			return 0;
		} finally {
			em.close();
		}
	}

	@Override
	public void deleteAll() {
		log.trace("delete all instances of {}", entityType);
		deleteAll(getAll());
	}

	protected Query query() {
		return Query.on(db);
	}

	public void detach(T val) {
		EntityManager em = db.newEntityManager();
		try {
			em.detach(val);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while detaching entity "
					+ entityType.getSimpleName(), e);
		} finally {
			em.close();
		}
	}

}
