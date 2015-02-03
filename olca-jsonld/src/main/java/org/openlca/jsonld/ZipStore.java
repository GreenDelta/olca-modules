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
	public void put(ModelType type, JsonObject object) {
		String refId = getRefId(object);
		if (type == null || refId == null)
			return;
		try {
			String json = new Gson().toJson(object);
			byte[] bytes = json.getBytes("utf-8");
			String dirName = ModelPath.get(type);
			Path dir = zip.getPath(dirName);
			if (!Files.exists(dir))
				Files.createDirectory(dir);
			Path path = zip.getPath(dirName + "/" + refId + ".json");
			Files.write(path, bytes, StandardOpenOption.CREATE);
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
	public JsonObject get(ModelType type, String refId) {
		if (!contains(type, refId))
			return null;
		String dirName = ModelPath.get(type);
		Path dir = zip.getPath(dirName);
		if (!Files.exists(dir))
			return null;
		Path path = zip.getPath(dirName + "/" + refId + ".json");
		if (!Files.exists(path))
			return null;
		try {
			return readJson(path);
		} catch (Exception e) {
			log.error("failed to read json object " + type + " " + refId, e);
			return null;
		}
	}

	private JsonObject readJson(Path path) throws Exception {
		byte[] bytes = Files.readAllBytes(path);
		String json = new String(bytes, "utf-8");
		JsonElement elem = new Gson().fromJson(json, JsonElement.class);
		if (!elem.isJsonObject())
			return null;
		else
			return elem.getAsJsonObject();
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		String dirName = ModelPath.get(type);
		Path dir = zip.getPath(dirName);
		if(!Files.exists(dir))
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
