package org.openlca.core.model.doc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Converter
public class ReviewScopeConverter
		implements AttributeConverter<List<ReviewScope>, String> {

	public static Optional<JsonArray> toJson(List<ReviewScope> scopes) {
		if (scopes == null || scopes.isEmpty())
			return Optional.empty();
		var array = new JsonArray(scopes.size());
		for (var scope : scopes) {
			array.add(scope.toJson());
		}
		return Optional.of(array);
	}

	public static List<ReviewScope> fromJson(JsonElement json) {
		if (json == null || !json.isJsonArray())
			return List.of();
		var array = json.getAsJsonArray();
		var list = new ArrayList<ReviewScope>(array.size());
		for (var e : array) {
			ReviewScope.fromJson(e).ifPresent(list::add);
		}
		return list;
	}

	@Override
	public String convertToDatabaseColumn(List<ReviewScope> scopes) {
		var array = toJson(scopes).orElse(null);
		return array != null
				? new Gson().toJson(array)
				: null;
	}

	@Override
	public List<ReviewScope> convertToEntityAttribute(String dbData) {
		var list = new ArrayList<ReviewScope>();
		if (Strings.nullOrEmpty(dbData))
			return list;
		try {
			var array = new Gson().fromJson(dbData, JsonArray.class);
			return fromJson(array);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse review scope", e);
		}
		return list;
	}

}
