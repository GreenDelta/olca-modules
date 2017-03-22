package org.openlca.jsonld;

import java.io.File;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.ModelType;
import org.openlca.jsonld.output.Context;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ZipStore implements EntityStore {

	private final static String CONTEXT_PATH = "context.json";
	private final Logger log = LoggerFactory.getLogger(getClass());
	private FileSystem zip;

	public static ZipStore open(File zipFile) throws IOException {
		return new ZipStore(zipFile);
	}

	private ZipStore(File zipFile) throws IOException {
		String uriStr = zipFile.toURI().toASCIIString();
		URI uri = URI.create("jar:" + uriStr);
		Map<String, String> options = new HashMap<>();
		if (!zipFile.exists())
			options.put("create", "true");
		zip = FileSystems.newFileSystem(uri, options);
		putContext();
	}

	@Override
	public void putContext() {
		JsonObject context = Context.write(Schema.URI);
		if (context == null)
			return;
		try {
			String json = new Gson().toJson(context);
			byte[] data = json.getBytes("utf-8");
			put(CONTEXT_PATH, data);
		} catch (Exception e) {
			log.error("failed to put " + CONTEXT_PATH, e);
		}
	}

	@Override
	public void putBin(ModelType type, String refId, String filename,
			byte[] data) {
		String path = ModelPath.getBin(type, refId) + "/" + filename;
		put(path, data);
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
	public void put(ModelType type, JsonObject object) {
		String refId = getRefId(object);
		if (type == null || refId == null)
			return;
		put(ModelPath.get(type, refId), object);
	}

	private void put(String path, JsonObject object) {
		try {
			String json = new Gson().toJson(object);
			byte[] data = json.getBytes("utf-8");
			put(path, data);
		} catch (Exception e) {
			log.error("failed to add " + path, e);
		}
	}

	private String getRefId(JsonObject obj) {
		if (obj == null)
			return null;
		JsonElement elem = obj.get("@id");
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
	}

	@Override
	public boolean contains(ModelType type, String refId) {
		if (type == null || refId == null)
			return false;
		String dirName = ModelPath.get(type);
		Path dir = zip.getPath(dirName);
		if (!Files.exists(dir))
			return false;
		Path path = zip.getPath(dirName + "/" + refId + ".json");
		return Files.exists(path);
	}

	@Override
	public JsonObject getContext() {
		byte[] data = get(CONTEXT_PATH);
		if (data == null)
			return null;
		try {
			return toJsonObject(data);
		} catch (Exception e) {
			log.error("failed to read json object " + CONTEXT_PATH, e);
			return null;
		}
	}

	@Override
	public byte[] get(String path) {
		if (Strings.nullOrEmpty(path))
			return null;
		try {
			Path file = zip.getPath(path);
			if (!Files.exists(file))
				return null;
			return Files.readAllBytes(file);
		} catch (Exception e) {
			log.error("failed to file " + path, e);
			return null;
		}
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		if (!contains(type, refId))
			return null;
		String path = ModelPath.get(type, refId);
		byte[] data = get(path);
		if (data == null)
			return null;
		try {
			return toJsonObject(data);
		} catch (Exception e) {
			log.error("failed to read json object " + type + "/" + refId, e);
			return null;
		}
	}

	private JsonObject toJsonObject(byte[] data) throws Exception {
		String json = new String(data, "utf-8");
		JsonElement e = new Gson().fromJson(json, JsonElement.class);
		return e.isJsonObject() ? e.getAsJsonObject() : null;
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		String dirName = ModelPath.get(type);
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
	public List<String> getBinFiles(ModelType type, String refId) {
		if (type == null || refId == null)
			return Collections.emptyList();
		Path dir = zip.getPath(ModelPath.getBin(type, refId));
		if (!Files.exists(dir))
			return Collections.emptyList();
		FilePathCollector collector = new FilePathCollector();
		try {
			Files.walkFileTree(dir, collector);
		} catch (Exception e) {
			log.error("failed to get bin files for " + type + ": " + refId, e);
		}
		return collector.paths;
	}

	@Override
	public void close() throws IOException {
		zip.close();
	}

	private class RefIdCollector extends SimpleFileVisitor<Path> {

		private List<String> ids = new ArrayList<>();

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			if (file == null)
				return FileVisitResult.CONTINUE;
			String fileName = file.getFileName().toString();
			String refId = fileName.substring(0, fileName.length() - 5);
			ids.add(refId);
			return FileVisitResult.CONTINUE;
		}
	}

	private class FilePathCollector extends SimpleFileVisitor<Path> {

		private List<String> paths = new ArrayList<>();

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			if (file == null)
				return FileVisitResult.CONTINUE;
			paths.add(file.toAbsolutePath().toString());
			return FileVisitResult.CONTINUE;
		}

	}
}
