package org.openlca.jsonld;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

import org.openlca.core.model.ModelType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/// ZipReader implements the JsonStoreReader interface and should be used when
/// reading from a zip package in the olca-schema format.
public class ZipReader implements JsonStoreReader, AutoCloseable {

	private final ZipFile zip;
	private List<String> _entries;

	private ZipReader(ZipFile zip) {
		this.zip = zip;
	}

	public static ZipReader of(File file) {
		try {
			var zip = new ZipFile(file);
			return new ZipReader(zip);
		} catch (Exception e) {
			throw new RuntimeException("failed to open zip", e);
		}
	}

	private List<String> entries() {
		if (_entries != null)
			return _entries;
		var list = new ArrayList<String>();
		for (var it = zip.entries(); it.hasMoreElements(); ) {
			var e = it.nextElement();
			list.add(e.getName());
		}
		_entries = list;
		return _entries;
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		var prefix = ModelPath.folderOf(type) + "/";
		var ids = new ArrayList<String>();
		for (var e : entries()) {
			if (!e.startsWith(prefix))
				continue;
			var file = e.substring(prefix.length());
			if (!file.endsWith(".json"))
				continue;
			var id = file.substring(0, file.length() - 5);
			ids.add(id);
		}
		return ids;
	}

	@Override
	public List<String> getFiles(String dir) {
		if (dir == null)
			return List.of();
		var prefix = dir.endsWith("/")
				? dir
				: dir + "/";
		var files = new ArrayList<String>();
		for (var e : entries()) {
			if (e.startsWith(prefix)) {
				files.add(e);
			}
		}
		return files;
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		var dir = ModelPath.binFolderOf(type, refId);
		return getFiles(dir);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		var file = ModelPath.jsonOf(type, refId);
		var json = getJson(file);
		return json != null
				? json.getAsJsonObject()
				: null;
	}

	@Override
	public List<JsonObject> getAll(ModelType type) {
		var ids = getRefIds(type);
		if (ids.isEmpty())
			return List.of();
		var list = new ArrayList<JsonObject>(ids.size());
		for (var id : ids) {
			var obj = Objects.requireNonNull(get(type, id));
			list.add(obj);
		}
		return list;
	}

	@Override
	public JsonElement getJson(String path) {
		var bytes = getBytes(path);
		if (bytes == null)
			return null;
		try (var stream = new ByteArrayInputStream(bytes);
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			return new Gson().fromJson(reader, JsonElement.class);
		} catch (IOException e) {
			throw new RuntimeException("failed to read Json: " + path, e);
		}
	}

	@Override
	public byte[] getBytes(String path) {
		if (path == null)
			return null;
		var e = zip.getEntry(path);
		if (e == null)
			return null;
		try (var stream = zip.getInputStream(e)) {
			return stream.readAllBytes();
		} catch (Exception ex) {
			throw new RuntimeException("failed to open entry: " + path, ex);
		}
	}

	@Override
	public void close() {
		try {
			zip.close();
		} catch (IOException e) {
			throw new RuntimeException("failed to close zip, e");
		}
	}
}
