package org.openlca.jsonld;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
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
	public void add(ModelType type, String refId, JsonObject object) {
		if (type == null || refId == null || object == null)
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
	public void close() throws IOException {
		zip.close();
	}
}
