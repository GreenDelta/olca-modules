package org.openlca.jsonld;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openlca.core.model.ModelType;
import org.openlca.jsonld.output.Context;
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

	private boolean allowCustomNames;

	public FileStore(File root) {
		this.root = root;
	}

	/**
	 * When setting this to true the file names of the data sets do not have to
	 * follow the pattern `<ref. ID>.json` but just need a JSON extension. This is
	 * ok for a small number of files but can lead to serious performace problems
	 * when there is a large number of files in the folder because in order to get
	 * the reference ID of a data set, it then needs to first parse its content.
	 */
	public FileStore withCustomNames(boolean b) {
		this.allowCustomNames = b;
		return this;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void putContext() {
		put("context.json", Context.write(Schema.URI));
	}

	@Override
	public JsonObject getContext() {
		return Context.write(Schema.URI);
	}

	@Override
	public void putMetaInfo(JsonObject info) {
		put("meta.json", info);
	}

	@Override
	public void put(ModelType type, JsonObject object) {
		if (type == null || object == null)
			return;
		var id = Json.getString(object, "@id");
		if (id == null)
			return;
		var path = ModelPath.get(type) + "/" + id + ".json";
		put(path, object);
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
		var file = find(type, refId);
		return file.isPresent();
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
		var file = find(type, refId);
		return file.isPresent()
				? readObject(file.get())
				: null;
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		if (type == null)
			return Collections.emptyList();
		var dir = new File(root, ModelPath.get(type));
		if (!dir.exists())
			return Collections.emptyList();

		if (!allowCustomNames)
			return Arrays.stream(dir.list())
					.filter(f -> f.endsWith(".json"))
					.map(f -> f.substring(0, f.length() - 5))
					.collect(Collectors.toList());

		return Arrays.stream(dir.listFiles())
				.parallel()
				.filter(file -> file.getName().endsWith(".json"))
				.map(file -> readObject(file))
				.map(json -> Json.getString(json, "@id"))
				.filter(id -> id != null)
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		var modelPath = ModelPath.getBin(type, refId);
		var dir = new File(root, modelPath);
		return !dir.exists()
				? Collections.emptyList()
				: Arrays.stream(dir.list())
						.map(file -> modelPath + "/" + file)
						.collect(Collectors.toList());
	}

	private Optional<File> find(ModelType type, String refID) {
		if (type == null || refID == null)
			return Optional.empty();
		var dir = new File(root, ModelPath.get(type));
		if (!dir.exists())
			return Optional.empty();

		if (!allowCustomNames) {
			var file = new File(dir, refID + ".json");
			return file.exists()
					? Optional.of(file)
					: Optional.empty();
		}

		return Arrays.stream(dir.listFiles())
				.parallel()
				.filter(file -> file.getName().endsWith(".json"))
				.filter(file -> {
					var json = readObject(file);
					var id = Json.getString(json, "@id");
					return id != null && id.equals(refID);
				})
				.findAny();
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
