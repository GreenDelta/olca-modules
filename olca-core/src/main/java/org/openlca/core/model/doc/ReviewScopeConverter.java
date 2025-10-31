package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

@Converter
public class ReviewScopeConverter
		implements AttributeConverter<ReviewScopeMap, String> {

	@Override
	public String convertToDatabaseColumn(ReviewScopeMap scopes) {
		return scopes != null && !scopes.isEmpty()
				? new Gson().toJson(scopes.toJson())
				: null;
	}

	@Override
	public ReviewScopeMap convertToEntityAttribute(String dbData) {
		if (Strings.isBlank(dbData))
			return new ReviewScopeMap();
		try {
			var array = new Gson().fromJson(dbData, JsonArray.class);
			return ReviewScopeMap.fromJson(array);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse review scope", e);
			return new ReviewScopeMap();
		}
	}
}
