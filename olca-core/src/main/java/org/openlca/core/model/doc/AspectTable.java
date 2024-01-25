package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Converter
public class AspectTable implements
		AttributeConverter<Map<String, String>, String> {

	public static JsonArray toJson(Map<String, String> map) {
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

	public static Map<String, String> fromJson(JsonElement e) {
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

	@Override
	public String convertToDatabaseColumn(Map<String, String> map) {
		return map != null && !map.isEmpty()
				? new Gson().toJson(toJson(map))
				: null;
	}

	@Override
	public Map<String, String> convertToEntityAttribute(String dbData) {
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
