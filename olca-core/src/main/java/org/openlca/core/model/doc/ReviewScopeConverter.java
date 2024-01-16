package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Convert
public class ReviewScopeConverter
		implements AttributeConverter<List<ReviewScope>, String> {

	@Override
	public String convertToDatabaseColumn(List<ReviewScope> scopes) {
		if (scopes == null || scopes.isEmpty())
			return null;
		var array = new JsonArray(scopes.size());
		for (var scope : scopes) {
			array.add(scope.toJson());
		}
		return new Gson().toJson(array);
	}

	@Override
	public List<ReviewScope> convertToEntityAttribute(String dbData) {
		var list = new ArrayList<ReviewScope>();
		if (Strings.nullOrEmpty(dbData))
			return list;
		try {
			var array = new Gson().fromJson(dbData, JsonArray.class);
			for (var e : array) {
				ReviewScope.fromJson(e).ifPresent(list::add);
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse review scope", e);
		}
		return list;
	}

}
