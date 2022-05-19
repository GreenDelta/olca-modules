package org.openlca.jsonld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;

/**
 * A simple implementation of the {@link JsonStoreReader} and
 * {@link JsonStoreWriter} interfaces that stores data in memory.
 */
public class MemStore implements JsonStoreReader, JsonStoreWriter {

	private final Map<String, JsonElement> jsonData = new HashMap<>();
	private final Map<String, byte[]> byteData = new HashMap<>();

	public MemStore() {
		PackageInfo.create().writeTo(this);
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		var prefix = ModelPath.folderOf(type) + '/';
		var ids = new ArrayList<String>();
		for (var path : jsonData.keySet()) {
			if (!path.startsWith(prefix) || !path.endsWith(".json"))
				continue;
			var id = path.substring(prefix.length(), path.length() - 5);
			ids.add(id);
		}
		return ids;
	}

	@Override
	public List<String> getFiles(String dir) {
		if (dir == null)
			return Collections.emptyList();
		var prefix = !dir.endsWith("/")
			? dir + "/"
			: dir;

		// first try Json objects
		var list = jsonData.keySet()
			.stream()
			.filter(path -> path.startsWith(prefix))
			.toList();
		if (!list.isEmpty())
			return list;

		// then try binary objects
		return byteData.keySet().stream()
			.filter(path -> path.startsWith(prefix))
			.toList();
	}

	@Override
	public List<String> getBinFiles(ModelType type, String refId) {
		var prefix = ModelPath.binFolderOf(type, refId) + '/';
		return byteData.keySet().stream()
			.filter(p -> p.startsWith(prefix))
			.toList();
	}

	@Override
	public JsonElement getJson(String path) {
		return jsonData.get(path);
	}

	@Override
	public byte[] getBytes(String path) {
		return byteData.get(path);
	}

	@Override
	public void put(String path, JsonObject object) {
		jsonData.put(path, object);
	}

	@Override
	public void put(String path, byte[] bytes) {
		byteData.put(path, bytes);
	}

	public void clear() {
		jsonData.clear();
		byteData.clear();
	}
}
