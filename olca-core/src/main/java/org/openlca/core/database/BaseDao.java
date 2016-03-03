package org.openlca.core.database;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDao<T> implements IDao<T> {

	/**
	 * A database dependent field for the maximum size of lists in JPQL queries.
	 */
	static final int MAX_LIST_SIZE = 1000;

	protected Class<T> entityType;
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private IDatabase database;

	public BaseDao(Class<T> entityType, IDatabase database) {
		this.entityType = entityType;
		this.database = database;
	}

	Class<T> getEntityType() {
		return entityType;
	}

	protected IDatabase getDatabase() {
		return database;
	}

	@Override
	public boolean contains(long id) {
		return getForId(id) != null;
	}

	@Override
	public void delete(T entity) {
		if (entity == null)
			return;
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			em.remove(em.merge(entity));
			em.getTransaction().commit();
			database.notifyDelete(entity);
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
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			for (T entity : entities) {
				log.trace("About to remove entity {}", entity);
				em.remove(em.merge(entity));
			}
			em.getTransaction().commit();
			for (T entity : entities) 
				database.notifyDelete(entity);
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
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			T retval = em.merge(entity);
			em.getTransaction().commit();
			database.notifyUpdate(entity);
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
		EntityManager em = createManager();
		try {
			em.getTransaction().begin();
			em.persist(entity);
			em.getTransaction().commit();
			database.notifyInsert(entity);
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
		EntityManager entityManager = createManager();
		try {
			T o = entityManager.find(entityType, id);
			return o;
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
		if (ids.size() <= MAX_LIST_SIZE)
			return fetchForIds(ids);
		return fetchForChunkedIds(ids);
	}

	private List<T> fetchForChunkedIds(Set<Long> ids) {
		List<Long> rest = new ArrayList<>(ids);
		List<T> results = new ArrayList<>();
		while (!rest.isEmpty()) {
			int toPos = rest.size() > MAX_LIST_SIZE ? MAX_LIST_SIZE : rest
					.size();
			List<Long> nextChunk = rest.subList(0, toPos);
			List<T> chunkResults = fetchForIds(nextChunk);
			results.addAll(chunkResults);
			nextChunk.clear(); // clears also the elements in rest
		}
		return results;
	}

	private List<T> fetchForIds(Collection<Long> ids) {
		EntityManager em = createManager();
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

	@Override
	public List<T> getAll() {
		log.debug("Select all for class {}", entityType);
		EntityManager em = createManager();
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
	public List<T> getAll(String jpql, Map<String, ? extends Object> parameters) {
		EntityManager em = createManager();
		try {
			TypedQuery<T> query = em.createQuery(jpql, entityType);
			for (String param : parameters.keySet()) {
				query.setParameter(param, parameters.get(param));
			}
			List<T> results = query.getResultList();
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
	public T getFirst(String jpql, Map<String, ? extends Object> parameters) {
		List<T> list = getAll(jpql, parameters);
		if (list.isEmpty())
			return null;
		return list.get(0);
	}

	@Override
	public long getCount(String jpql, Map<String, Object> parameters) {
		EntityManager em = createManager();
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

	protected EntityManager createManager() {
		EntityManager em = getDatabase().getEntityFactory()
				.createEntityManager();
		return em;
	}

	protected Query query() {
		return Query.on(database);
	}

	protected List<Object[]> selectAll(String sql, String[] fields,
			List<Object> parameters) {
		try (Connection conn = getDatabase().createConnection()) {
			List<Object[]> results = execute(sql, fields, parameters, conn,
					false);
			return results;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to execute query: "
					+ sql, e);
			return Collections.emptyList();
		}
	}

	protected Object[] selectFirst(String sql, String[] fields,
			List<Object> parameters) {
		try (Connection conn = getDatabase().createConnection()) {
			List<Object[]> results = execute(sql, fields, parameters, conn,
					true);
			if (results.isEmpty())
				return null;
			return results.get(0);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "failed to execute query: "
					+ sql, e);
			return null;
		}
	}

	private List<Object[]> execute(String sql, String[] fields,
			List<Object> parameters, Connection conn, boolean single)
			throws SQLException {
		List<Object[]> results = new ArrayList<>();
		PreparedStatement statement = conn.prepareStatement(sql);
		for (int i = 0; i < parameters.size(); i++)
			statement.setObject(i + 1, parameters.get(i));
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			Object[] result = new Object[fields.length];
			for (int i = 0; i < fields.length; i++)
				result[i] = getValue(resultSet, fields[i]);
			results.add(result);
			if (single)
				break;
		}
		resultSet.close();
		statement.close();
		return results;
	}

	private Object getValue(ResultSet resultSet, String field)
			throws SQLException {
		Object value = resultSet.getObject(field);
		if (value instanceof Clob)
			value = ((Clob) value).getSubString(1,
					(int) ((Clob) value).length());
		return value;
	}

	public T detach(T val) {
		EntityManager em = createManager();
		try {
			em.detach(val);
			return val;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log, "Error while detaching entity "
					+ entityType.getSimpleName(), e);
			return val;
		} finally {
			em.close();
		}
	}

}
