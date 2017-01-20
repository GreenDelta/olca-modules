package org.openlca.ilcd.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipStore implements DataStore {

	private Logger log = LoggerFactory.getLogger(getClass());
	private FileSystem zip;
	private HashMap<String, List<Path>> entries;
	private XmlBinder binder = new XmlBinder();

	public ZipStore(File zipFile) throws IOException {
		log.trace("Create zip store {}", zipFile);
		String uriStr = zipFile.toURI().toASCIIString();
		URI uri = URI.create("jar:" + uriStr);
		Map<String, String> options = new HashMap<>();
		if (!zipFile.exists())
			options.put("create", "true");
		zip = FileSystems.newFileSystem(uri, options);
		initEntries();
	}

	private void initEntries() throws IOException {
		entries = new HashMap<>();
		for (Path root : zip.getRootDirectories()) {
			Files.walkFileTree(root, new FileVisitor(f -> {
				Path p = f.getParent();
				if (p == null)
					return;
				Path dirPath = p.getFileName();
				if (dirPath == null)
					return;
				String dir = dirPath.toString().toLowerCase();
				List<Path> list = getEntries(dir);
				list.add(f);
			}));
		}
	}

	/** Get the entries of the given directory. */
	public List<Path> getEntries(String dir) {
		List<Path> list = entries.get(dir);
		if (list == null) {
			list = new ArrayList<>();
			entries.put(dir, list);
		}
		return list;
	}

	public List<Path> getEntries(Class<? extends IDataSet> type) {
		return getEntries(Dir.get(type));
	}

	@Override
	public <T> T get(Class<T> type, String id) throws DataStoreException {
		log.trace("Get {} for id {} from zip", type, id);
		Path entry = findEntry(Dir.get(type), id);
		if (entry == null)
			return null;
		return unmarshal(type, entry);
	}

	@Override
	public void put(IDataSet ds) throws DataStoreException {
		log.trace("Store {} in zip.", ds);
		if (ds == null)
			return;
		String dir = Dir.get(ds.getClass());
		String entryName = "ILCD" + "/" + dir + "/" + ds.getUUID() + ".xml";
		try {
			Path entry = zip.getPath(entryName);
			Path parent = entry.getParent();
			if (parent != null && !Files.exists(parent))
				Files.createDirectories(parent);
			OutputStream os = Files.newOutputStream(entry);
			binder.toStream(ds, os);
			List<Path> list = getEntries(dir);
			list.add(entry);
		} catch (Exception e) {
			throw new DataStoreException("Could not add file  " + entryName, e);
		}
	}

	@Override
	public void put(Source source, File[] files)
			throws DataStoreException {
		log.trace("Store source {} with digital files", source);
		put(source);
		if (files == null || files.length == 0)
			return;
		try {
			Path parent = zip.getPath("ILCD/external_docs");
			if (parent != null && !Files.exists(parent))
				Files.createDirectories(parent);
			for (File file : files) {
				Path entry = zip.getPath("ILCD/external_docs/" + file.getName());
				Files.copy(file.toPath(), entry,
						StandardCopyOption.REPLACE_EXISTING);
				List<Path> list = getEntries("external_docs");
				list.add(entry);
			}
		} catch (Exception e) {
			throw new DataStoreException("Could not store digital files", e);
		}
	}

	@Override
	public InputStream getExternalDocument(String sourceId, String file)
			throws DataStoreException {
		log.trace("Get external document {}", file);
		Path entry = findEntry("external_docs", file);
		if (entry == null)
			return null;
		try {
			return Files.newInputStream(entry);
		} catch (Exception e) {
			throw new DataStoreException("failed to open file " + file, e);
		}
	}

	@Override
	public <T> boolean delete(Class<T> type, String id)
			throws DataStoreException {
		throw new UnsupportedOperationException("delete in zips not supported");
	}

	<T> T unmarshal(Class<T> type, Path entry) throws DataStoreException {
		try {
			InputStream is = Files.newInputStream(entry);
			T t = binder.fromStream(type, is);
			return t;
		} catch (Exception e) {
			throw new DataStoreException("Cannot load " + type + " from entry "
					+ entry, e);
		}
	}

	private Path findEntry(String dir, String id) {
		List<Path> list = entries.get(dir);
		if (list == null)
			return null;
		for (Path entry : list) {
			String name = entry.getFileName().toString();
			if (name.contains(id))
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
		return findEntry(Dir.get(type), id) != null;
	}

	@Override
	public void close() throws DataStoreException {
		log.trace("close zip store");
		if (entries == null)
			return;
		entries = null;
		try {
			zip.close();
		} catch (Exception e) {
			throw new DataStoreException("Could not close ZipStore", e);
		}
	}

	private class FileVisitor extends SimpleFileVisitor<Path> {

		private Consumer<Path> fn;

		public FileVisitor(Consumer<Path> fn) {
			this.fn = fn;
		}

		@Override
		public FileVisitResult visitFile(Path f, BasicFileAttributes atts)
				throws IOException {
			if (f == null || f.getParent() == null)
				return FileVisitResult.CONTINUE;
			fn.accept(f);
			return FileVisitResult.CONTINUE;
		}
	}

}
