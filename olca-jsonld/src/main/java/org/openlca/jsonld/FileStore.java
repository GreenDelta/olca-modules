package org.openlca.jsonld;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

/**
 * An implementation of the EntityStore interface that maps data sets to files
 * in a folder.
 */
public class FileStore implements EntityStore {

	/**
	 * The root folder of the file store. All content is stored in the sub-folders
	 * of this directory.
	 */
	public final File root;

	public FileStore(File root) {
		this.root = root;
	}

	@Override
	public void putContext() {
		// TODO Auto-generated method stub
	}

	@Override
	public void putMetaInfo(JsonObject info) {
		// TODO Auto-generated method stub
	}

	@Override
	public void putBin(ModelType type, String refId, String filename, byte[] data) {
		String path = ModelPath.getBin(type, refId) + "/" + filename;
		put(path, data);
	}

	@Override
	public void put(String path, byte[] data) {
		if (Strings.nullOrEmpty(path) || data == null)
			return;
		try {
			var file = new File(root, path);
			var dir = file.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			Files.write(file.toPath(), data,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to write file " + path, e);
		}
	}

	private void put(String path, JsonObject object) {
		if (object == null)
			return;
		try {
			var json = new Gson().toJson(object);
			byte[] data = json.getBytes("utf-8");
			put(path, data);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"failed to encode JSON object @" + path, e);
		}
	}

	@Override
	public boolean contains(ModelType type, String refId) {
		if (type == null || refId == null)
			return false;
		var dir = new File(root, ModelPath.get(type));
		if (!dir.exists())
			return false;
		var file = new File(dir, refId + ".json");
		return file.exists();
	}

	@Override
	public byte[] get(String path) {
		if (Strings.nullOrEmpty(path))
			return null;
		try {
			var file = new File(root, path);
			if (!file.exists())
				return null;
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to read @" + path, e);
		}
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		if (type == null || refId == null)
			return null;
		var dir = new File(root, ModelPath.get(type));
		if (!dir.exists())
			return null;
		var file = new File(dir, refId + ".json");
		return file.exists()
				? readObject(file)
				: null;
	}

	private JsonObject readObject(File file) {
		if (file == null || !file.exists())
			return null;
		try (var stream = new FileInputStream(file);
				var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				var buffer = new BufferedReader(reader)) {
			return new Gson().fromJson(buffer, JsonObject.class);
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to read bytes into JSON object", e);
		}
	}

}
