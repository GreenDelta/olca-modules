package org.openlca.core.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDao<T> {

	boolean contains(long id);

	Map<Long, Boolean> contains(Set<Long> ids);

	void delete(T entity);

	void deleteAll(Collection<T> entities);

	T update(T entity);

	T insert(T entity);

	T getForId(long id);

	List<T> getForIds(Set<Long> ids);

	List<T> getAll();

	List<T> getAll(String jpql, Map<String, ? extends Object> parameters);

	T getFirst(String jpql, Map<String, ? extends Object> parameters);

	long getCount(String jpql, Map<String, Object> parameters);

	void deleteAll();

}