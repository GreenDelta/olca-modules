package org.openlca.jsonld;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipStore implements JsonStoreWriter, JsonStoreReader, AutoCloseable {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final FileSystem zip;

	public static ZipStore open(File zipFile) throws IOException {
		return new ZipStore(zipFile);
	}

	private ZipStore(File zipFile) throws IOException {
		String uriStr = zipFile.toURI().toASCIIString();
		URI uri = URI.create("jar:" + uriStr);
		boolean create = !zipFile.exists();
		zip = create
				? FileSystems.newFileSystem(uri, Map.of("create", "true"))
				: FileSystems.newFileSystem(uri, Map.of());
		if (create) {
			PackageInfo.create().writeTo(this);
		}
	}

	@Override
	public void put(String path, byte[] data) {
		if (Strings.nullOrEmpty(path) || data == null)
			return;
		try {
			Path file = zip.getPath(path);
			Path dir = file.getParent();
			if (dir != null && !(Files.exists(dir)))
				Files.createDirectories(dir);
			Files.write(file, data, StandardOpenOption.CREATE);
		} catch (Exception e) {
			log.error("failed to put " + path, e);
		}
	}

	@Override
	public byte[] getBytes(String path) {
		if (Strings.nullOrEmpty(path))
			return null;
		try {
			Path file = zip.getPath(path);
			if (!Files.exists(file))
				return null;
			return Files.readAllBytes(file);
		} catch (Exception e) {
			log.error("failed to get file " + path, e);
			return null;
		}
	}

	public InputStream getStream(String path) {
		if (Strings.nullOrEmpty(path))
			return null;
		Path file = zip.getPath(path);
		if (!Files.exists(file))
			return null;
		try {
			return Files.newInputStream(file);
		} catch (Exception e) {
			log.error("failed to open stream for path " + path, e);
			return null;
		}
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		String dirName = ModelPath.folderOf(type);
		Path dir = zip.getPath(dirName);
		if (!Files.exists(dir))
			return Collections.emptyList();
		RefIdCollector collector = new RefIdCollector();
		try {
			Files.walkFileTree(dir, collector);
		} catch (Exception e) {
			log.error("failed to get refIds for type " + type, e);
		}
		return collector.ids;
	}

	@Override
	public void close() throws IOException {
		zip.close();
	}

	/**
	 * Returns the paths of the files that are located under the given folder.
	 * The returned paths are absolute to the root of the underlying zip file.
	 * Thus, you can get the content of such a path `p` by using the method
	 * `get(p)`.
	 */
	@Override
	public List<String> getFiles(String folder) {
		if (folder == null)
			return Collections.emptyList();
		Path dir = zip.getPath(folder);
		if (!Files.exists(dir))
			return Collections.emptyList();
		List<String> paths = new ArrayList<>();
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					paths.add(file.toAbsolutePath().toString());
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Exception e) {
			log.error("failed collect files from " + folder, e);
		}
		return paths;
	}

	private static class RefIdCollector extends SimpleFileVisitor<Path> {

		private final List<String> ids = new ArrayList<>();

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			if (file == null)
				return FileVisitResult.CONTINUE;
			String fileName = file.getFileName().toString();
			String refId = fileName.substring(0, fileName.length() - 5);
			ids.add(refId);
			return FileVisitResult.CONTINUE;
		}
	}

}
