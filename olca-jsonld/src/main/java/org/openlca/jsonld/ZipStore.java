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
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ZipStore implements EntityStore {

	private Logger log = LoggerFactory.getLogger(getClass());

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
		try {
			String json = new Gson().toJson(object);
			byte[] data = json.getBytes("utf-8");
			String dirName = ModelPath.get(type);
			String path = dirName + "/" + refId + ".json";
			put(path, data);
		} catch (Exception e) {
			log.error("failed to add " + type + "/" + refId, e);
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
		String path = ModelPath.get(type) + "/" + refId + ".json";
		byte[] data = get(path);
		if (data == null)
			return null;
		try {
			String json = new String(data, "utf-8");
			JsonElement e = new Gson().fromJson(json, JsonElement.class);
			return e.isJsonObject() ? e.getAsJsonObject() : null;
		} catch (Exception e) {
			log.error("failed to read json object " + type + " " + refId, e);
			return null;
		}
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
	public void close() throws IOException {
		zip.close();
	}

	@Override
	public JsonObject initJson() {
		JsonObject obj = new JsonObject();
		Context.add(obj);
		return obj;
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
}
