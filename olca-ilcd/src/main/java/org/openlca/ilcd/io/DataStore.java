package org.openlca.ilcd.io;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DataStore extends Closeable {

	<T extends IDataSet> T get(Class<T> type, String id);

	InputStream getExternalDocument(String sourceId, String fileName);

	void put(IDataSet ds);

	void put(Source source, File[] files);

	<T extends IDataSet> boolean delete(Class<T> type, String id);

	<T extends IDataSet> Iterator<T> iterator(Class<T> type);

	<T extends IDataSet> boolean contains(Class<T> type, String id);

	default <T extends IDataSet> void each(Class<T> type, Consumer<T> fn) {
		try {
			Iterator<T> it = iterator(type);
			while (it.hasNext()) {
				T t = it.next();
				if (t != null) {
					fn.accept(t);
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to iterate over elements of type " + type, e);
		}
	}
}
