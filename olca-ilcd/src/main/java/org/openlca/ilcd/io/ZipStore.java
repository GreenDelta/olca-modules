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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipStore implements DataStore {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final FileSystem zip;
	private HashMap<String, List<Path>> entries;

	/**
	 * Contains the IDs that where added to this zip store. This is used to make
	 * the contains check faster because in the export we usually first call
	 * contains and then do the export if necessary.
	 */
	private final HashMap<Class<?>, Set<String>> addedContent = new HashMap<>();

	private final XmlBinder binder = new XmlBinder();

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
		return entries.computeIfAbsent(dir, _d -> new ArrayList<>());
	}

	public List<Path> getEntries(Class<? extends IDataSet> type) {
		return getEntries(Dir.get(type));
	}

	@Override
	public <T extends IDataSet> T get(Class<T> type, String id) {
		log.trace("Get {} for id {} from zip", type, id);
		Path entry = findEntry(Dir.get(type), id);
		if (entry == null)
			return null;
		return unmarshal(type, entry);
	}

	@Override
	public void put(IDataSet ds) {
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
			var ids = addedContent.computeIfAbsent(
					ds.getClass(), k -> new HashSet<>());
			ids.add(ds.getUUID());
		} catch (Exception e) {
			throw new RuntimeException("Could not add file  " + entryName, e);
		}
	}

	@Override
	public void put(Source source, File[] files) {
		log.trace("Store source {} with digital files", source);
		put(source);
		if (files == null || files.length == 0)
			return;
		try {
			Path parent = zip.getPath("ILCD/external_docs");
			if (!Files.exists(parent)) {
				Files.createDirectories(parent);
			}
			for (File file : files) {
				Path entry = zip.getPath("ILCD/external_docs/" + file.getName());
				Files.copy(file.toPath(), entry,
						StandardCopyOption.REPLACE_EXISTING);
				List<Path> list = getEntries("external_docs");
				list.add(entry);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not store digital files", e);
		}
	}

	@Override
	public InputStream getExternalDocument(String sourceId, String file) {
		log.trace("Get external document {}", file);
		Path entry = findEntry("external_docs", file);
		if (entry == null)
			return null;
		try {
			return Files.newInputStream(entry);
		} catch (Exception e) {
			throw new RuntimeException("failed to open file " + file, e);
		}
	}

	@Override
	public <T extends IDataSet> boolean delete(Class<T> type, String id) {
		throw new UnsupportedOperationException("delete in zips not supported");
	}

	<T> T unmarshal(Class<T> type, Path entry) {
		try {
			InputStream is = Files.newInputStream(entry);
			return binder.fromStream(type, is);
		} catch (Exception e) {
			throw new RuntimeException("Cannot load " + type + " from entry "
					+ entry, e);
		}
	}

	private Path findEntry(String dir, String id) {
		List<Path> list = entries.get(dir);
		if (list == null)
			return null;
		for (Path entry : list) {
			try {
				String name = entry.getFileName().toString();
				if (name.contains(id))
					return entry;
			} catch (Exception e) {
				// an exception can occur when getting the string
				// representation of an entry in older JDK
				// versions:https://bugs.openjdk.java.net/browse/JDK-8061777
				// log.warn("Could not read zip entry {}", entry);
			}
		}
		return null;
	}

	@Override
	public <T extends IDataSet> Iterator<T> iterator(Class<T> type) {
		log.trace("create iterator for type {}", type);
		return new ZipEntryIterator<>(this, type);
	}

	@Override
	public <T extends IDataSet> boolean contains(Class<T> type, String id) {
		Set<String> ids = addedContent.get(type);
		if (ids != null && ids.contains(id))
			return true;
		Path entry = findEntry(Dir.get(type), id);
		if (entry == null)
			return false;
		if (ids == null) {
			ids = new HashSet<>();
			addedContent.put(type, ids);
		}
		ids.add(id);
		return true;
	}

	@Override
	public void close() {
		log.trace("close zip store");
		if (entries == null)
			return;
		entries = null;
		try {
			zip.close();
		} catch (Exception e) {
			throw new RuntimeException("Could not close ZipStore", e);
		}
	}

	private static class FileVisitor extends SimpleFileVisitor<Path> {

		private final Consumer<Path> fn;

		public FileVisitor(Consumer<Path> fn) {
			this.fn = fn;
		}

		@Override
		public FileVisitResult visitFile(Path f, BasicFileAttributes atts) {
			if (f == null || f.getParent() == null)
				return FileVisitResult.CONTINUE;
			fn.accept(f);
			return FileVisitResult.CONTINUE;
		}
	}

}
