package org.openlca.jsonld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.core.model.ModelType;

import com.google.gson.JsonObject;

/**
 * A simple implementation of the {@link JsonStoreWriter} interface that stores the
 * JSON objects in memory. Not all features of a real file based entity store
 * are supported.
 */
public class MemStore implements JsonStoreWriter {

	private HashMap<ModelType, List<JsonObject>> data = new HashMap<>();

	@Override
	public void put(ModelType type, JsonObject obj) {
		if (type == null || obj == null)
			return;
		List<JsonObject> list = data.computeIfAbsent(type,
				t -> new ArrayList<>(1));
		list.add(obj);
	}

	@Override
	public boolean contains(ModelType type, String id) {
		return get(type, id) != null;
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		List<JsonObject> models = data.get(type);
		if (models == null)
			return Collections.emptyList();
		List<String> ids = new ArrayList<>(models.size());
		for (JsonObject obj : models) {
			String id = Json.getString(obj, "@id");
			if (id != null) {
				ids.add(id);
			}
		}
		return ids;
	}

	@Override
	public JsonObject get(ModelType type, String id) {
		List<JsonObject> models = data.get(type);
		if (models == null)
			return null;
		for (JsonObject obj : models) {
			String objId = Json.getString(obj, "@id");
			if (Objects.equals(id, objId))
				return obj;
		}
		return null;
	}

	public List<JsonObject> getAll(ModelType type) {
		List<JsonObject> models = data.get(type);
		return models == null ? Collections.emptyList() : models;
	}

	@Override
	public void putBin(ModelType type, String s, String s1, byte[] bytes) {
	}

	@Override
	public void put(String path, byte[] bytes) {
	}

	@Override
	public byte[] get(String path) {
		return new byte[0];
	}

	@Override
	public void putContext() {
	}

	@Override
	public void putMetaInfo(JsonObject info) {
	}

	@Override
	public JsonObject getContext() {
		JsonObject obj = new JsonObject();
		obj.addProperty("@vocab", "http://openlca.org/schema/v1.1/");
		return obj;
	}

	@Override
	public List<String> getBinFiles(ModelType type, String id) {
		return Collections.emptyList();
	}

	@Override
	public void close() {
	}
}
