package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Converts a map into a JSON array of objects with key-value-pairs,
 * and the other way around.
 */
class SimpleMapConverter {

	private final String keySlot;
	private final String valSlot;

	SimpleMapConverter(String keySlot, String valSlot) {
		this.keySlot = keySlot;
		this.valSlot = valSlot;
	}

	public Optional<JsonArray> toJson(Map<String, String> map) {
		if (map == null || map.isEmpty())
			return Optional.empty();
		var array = new JsonArray(map.size());
		for (var e : map.entrySet()) {
			if (Strings.nullOrEmpty(e.getKey())
					|| Strings.nullOrEmpty(e.getValue()))
				continue;
			var obj = new JsonObject();
			Json.put(obj, keySlot, e.getKey());
			Json.put(obj, valSlot, e.getValue());
			array.add(obj);
		}
		return Optional.of(array);
	}

	public Map<String, String> fromJson(JsonElement e) {
		if (e == null || !e.isJsonArray())
			return Map.of();
		var array = e.getAsJsonArray();
		var map = new HashMap<String, String>(array.size());
		for (var i : array) {
			if (!i.isJsonObject())
				continue;
			var obj = i.getAsJsonObject();
			var key = Json.getString(obj, keySlot);
			var val = Json.getString(obj, valSlot);
			if (Strings.nullOrEmpty(key) || Strings.nullOrEmpty(val))
				continue;
			map.put(key, val);
		}
		return map;
	}

	String convertToDatabaseColumn(Map<String, String> map) {
		var array = toJson(map).orElse(null);
		return array != null
				? new Gson().toJson(array)
				: null;
	}

	Map<String, String> convertToEntityAttribute(String dbData) {
		var map = new HashMap<String, String>();
		if (Strings.nullOrEmpty(dbData))
			return map;
		try {
			var array = new Gson().fromJson(dbData, JsonArray.class);
			map.putAll(fromJson(array));
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse map", e);
		}
		return map;
	}
}

