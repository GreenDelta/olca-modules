package org.openlca.core.model.doc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import java.util.HashMap;
import java.util.Map;

class Util {

	private Util() {
	}

	static JsonArray toJson(Map<String, String> map) {
		if (map == null || map.isEmpty())
			return new JsonArray();
		var array = new JsonArray(map.size());
		for (var e : map.entrySet()) {
			if (Strings.nullOrEmpty(e.getKey())
					|| Strings.nullOrEmpty(e.getValue()))
				continue;
			var obj = new JsonObject();
			Json.put(obj, "aspect", e.getKey());
			Json.put(obj, "value", e.getValue());
			array.add(obj);
		}
		return array;
	}

	static Map<String, String> parseMap(JsonElement e) {
		if (e == null || !e.isJsonArray())
			return Map.of();
		var array = e.getAsJsonArray();
		var map = new HashMap<String, String>(array.size());
		for (var i : array) {
			if (!i.isJsonObject())
				continue;
			var obj = i.getAsJsonObject();
			var key = Json.getString(obj, "aspect");
			var val = Json.getString(obj, "value");
			if (Strings.nullOrEmpty(key) || Strings.nullOrEmpty(val))
				continue;
			map.put(key, val);
		}
		return map;
	}
}
