package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Converter
public class ComplianceAspectConverter implements
		AttributeConverter<Map<String, String>, String> {

	public static JsonArray toJson(Map<String, String> map) {
		return Util.toJson(map);
	}

	public static Map<String, String> fromJson(JsonElement e) {
		return Util.parseMap(e);
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
