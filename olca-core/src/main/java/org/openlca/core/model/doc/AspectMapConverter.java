package org.openlca.core.model.doc;

import org.openlca.commons.Strings;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
