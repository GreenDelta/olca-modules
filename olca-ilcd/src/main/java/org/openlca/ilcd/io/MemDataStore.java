package org.openlca.ilcd.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.sources.Source;

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
	public void put(IDataSet ds) throws DataStoreException {
		if (ds == null)
			return;
		Class<?> clazz = ds.getClass();
		HashMap<String, Object> map = content.get(clazz);
		if (map == null) {
			map = new HashMap<>();
			content.put(clazz, map);
		}
		map.put(ds.getUUID(), ds);
	}

	@Override
	public void put(Source source, File[] files)
			throws DataStoreException {
		put(source);
		HashMap<String, Object> map = content.get(File.class);
		if (map == null) {
			map = new HashMap<>();
			content.put(File.class, map);
		}
		for (File file : files)
			map.put(file.getName(), file);
	}

	@Override
	public InputStream getExternalDocument(String sourceId, String fileName)
			throws DataStoreException {
		HashMap<String, Object> map = content.get(File.class);
		if (map == null)
			return null;
		try {
			File file = (File) map.get(fileName);
			if (file == null)
				return null;
			else
				return new FileInputStream(file);
		} catch (Exception e) {
			throw new DataStoreException("Could not open file " + fileName, e);
		}
	}

	@Override
	public <T> boolean delete(Class<T> type, String id)
			throws DataStoreException {
		HashMap<String, Object> map = content.get(type);
		if (map == null)
			return false;
		Object o = map.remove(id);
		return o != null;
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
