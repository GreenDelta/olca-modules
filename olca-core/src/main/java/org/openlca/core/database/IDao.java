package org.openlca.core.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDao<T> {

	boolean contains(long id) throws Exception;

	void delete(T entity) throws Exception;

	void deleteAll(Collection<T> entities) throws Exception;

	T update(T entity) throws Exception;

	T insert(T entity) throws Exception;

	T getForId(long id) throws Exception;

	List<T> getForIds(Set<Long> ids) throws Exception;

	List<T> getAll() throws Exception;

	List<T> getAll(String jpql, Map<String, ? extends Object> parameters)
			throws Exception;

	T getFirst(String jpql, Map<String, ? extends Object> parameters)
			throws Exception;

	long getCount(String jpql, Map<String, Object> parameters) throws Exception;

	void deleteAll() throws Exception;

}