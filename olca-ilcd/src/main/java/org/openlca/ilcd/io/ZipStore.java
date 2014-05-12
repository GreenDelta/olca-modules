package org.openlca.ilcd.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TFileOutputStream;
import de.schlichtherle.truezip.file.TVFS;

public class ZipStore implements DataStore {

	private Logger log = LoggerFactory.getLogger(getClass());
	private TFile dir;
	private List<TFile> entries = new ArrayList<>();
	private XmlBinder binder = new XmlBinder();

	public ZipStore(File zipFile) {
		log.trace("Create zip store {}", zipFile);
		dir = new TFile(zipFile);
		if (!dir.exists())
			dir.mkdirs();
		initEntryNames(dir);
	}

	private void initEntryNames(TFile dir) {
		log.trace("index entries");
		for (TFile file : dir.listFiles()) {
			String name = file.getName();
			if (!file.isDirectory() && name.toLowerCase().endsWith(".xml"))
				entries.add(file);
			else if (file.isDirectory())
				initEntryNames(file);
		}
		log.trace("{} xml files indexed", entries.size());
	}

	@Override
	public <T> T get(Class<T> type, String id) throws DataStoreException {
		log.trace("Get {} for id {} from zip", type, id);
		TFile entry = findEntry(Path.forClass(type), id);
		if (entry == null)
			return null;
		return unmarshal(type, entry);
	}

	@Override
	public void put(Object obj, String id) throws DataStoreException {
		log.trace("Store {} with id {} in zip.", obj, id);
		if (obj == null || id == null)
			return;
		String path = Path.forClass(obj.getClass());
		String entryName = "ILCD" + "/" + path + "/" + id + ".xml";
		TFile file = new TFile(dir, entryName);
		try {
			// impacts of closing output stream not clear
			TFileOutputStream fos = new TFileOutputStream(file);
			binder.toStream(obj, fos);
			entries.add(file);
		} catch (Exception e) {
			throw new DataStoreException("Could not create stream  " + file, e);
		}
	}

	@Override
	public <T> boolean delete(Class<T> type, String id)
			throws DataStoreException {
		throw new UnsupportedOperationException("delete in zips not supported");
	}

	// impacts of closing input stream not clear
	<T> T unmarshal(Class<T> type, TFile entry) throws DataStoreException {
		try {
			TFileInputStream is = new TFileInputStream(entry);
			T t = binder.fromStream(type, is);
			return t;
		} catch (Exception e) {
			throw new DataStoreException("Cannot load " + type + " from entry "
					+ entry.getName(), e);
		}
	}

	private TFile findEntry(String path, String id) {
		for (TFile entry : entries) {
			String name = entry.getAbsolutePath();
			if (name.contains(path) && name.contains(id))
				return entry;
		}
		return null;
	}

	@Override
	public <T> Iterator<T> iterator(Class<T> type) throws DataStoreException {
		log.trace("create iterator for type {}", type);
		return new ZipEntryIterator<>(this, type);
	}

	@Override
	public <T> boolean contains(Class<T> type, String id)
			throws DataStoreException {
		return findEntry(Path.forClass(type), id) != null;
	}

	List<TFile> findEntries(String path) {
		List<TFile> foundEntries = new ArrayList<>();
		for (TFile entry : entries) {
			String name = entry.getAbsolutePath();
			if (name.contains(path)) {
				foundEntries.add(entry);
			}
		}
		return foundEntries;
	}

	@Override
	public void close() throws DataStoreException {
		log.trace("close zip store");
		if (dir == null)
			return;
		entries.clear();
		try {
			TVFS.umount(dir);
			dir = null;
		} catch (Exception e) {
			throw new DataStoreException("Could not close ZipStore", e);
		}
	}

}
