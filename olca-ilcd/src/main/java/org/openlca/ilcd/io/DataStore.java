package org.openlca.ilcd.io;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.sources.Source;

public interface DataStore extends Closeable {

	<T> T get(Class<T> type, String id) throws DataStoreException;

	InputStream getExternalDocument(String sourceId, String fileName)
			throws DataStoreException;

	void put(IDataSet ds) throws DataStoreException;

	void put(Source source, File[] files) throws DataStoreException;

	<T> boolean delete(Class<T> type, String id) throws DataStoreException;

	<T> Iterator<T> iterator(Class<T> type) throws DataStoreException;

	<T> boolean contains(Class<T> type, String id) throws DataStoreException;

}
