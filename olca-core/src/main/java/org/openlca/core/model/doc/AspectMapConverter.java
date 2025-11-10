package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.checkerframework.checker.units.qual.A;
import org.openlca.jsonld.Json;
import org.openlca.commons.Strings;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Converter
public class AspectMapConverter implements AttributeConverter<AspectMap, String> {

	@Override
	public String convertToDatabaseColumn(AspectMap map) {
		return map != null && !map.isEmpty()
				? new Gson().toJson(map.toJson())
				: null;
	}

	@Override
	public AspectMap convertToEntityAttribute(String dbData) {
		if (Strings.isBlank(dbData))
			return new AspectMap();
		try {
			var array = new Gson().fromJson(dbData, JsonArray.class);
			return AspectMap.fromJson(array);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse map", e);
			return new AspectMap();
		}
	}
}
