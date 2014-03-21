package org.openlca.ilcd.io;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/** An in memory implementation of the data store interface. */
public class MemDataStore implements DataStore {

	private final HashMap<Class<?>, HashMap<String, Object>> content = new HashMap<>();

	@Override
	public <T> T get(Class<T> type, String id) throws DataStoreException {
		HashMap<String, Object> map = content.get(type);
		if (map == null)
			return null;
		Object dataSet = map.get(id);
		if (dataSet == null)
			return null;
		return type.cast(dataSet);
	}

	@Override
	public void put(Object obj, String id) throws DataStoreException {
		if (obj == null)
			return;
		Class<?> clazz = obj.getClass();
		HashMap<String, Object> map = content.get(clazz);
		if (map == null) {
			map = new HashMap<>();
			content.put(clazz, map);
		}
		map.put(id, obj);
	}

	@Override
	public <T> void delete(Class<T> type, String id) throws DataStoreException {
		HashMap<String, Object> map = content.get(type);
		if (map == null)
			return;
		map.remove(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Iterator<T> iterator(Class<T> type) throws DataStoreException {
		if (type == null)
			return Collections.emptyIterator();
		HashMap<String, Object> map = content.get(type);
		if (map == null)
			return Collections.emptyIterator();
		return (Iterator<T>) map.values().iterator();
	}

	@Override
	public <T> boolean contains(Class<T> type, String id)
			throws DataStoreException {
		HashMap<String, Object> map = content.get(type);
		if (map == null)
			return false;
		Object obj = map.get(id);
		return obj != null;
	}

	@Override
	public void close() throws IOException {
		content.clear();
	}

}
