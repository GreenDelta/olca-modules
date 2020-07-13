package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;
import org.openlca.util.Strings;

final class In {

	private In() {
	}

	static long getVersion(JsonObject obj) {
		if (obj == null)
			return 0;
		String version = Json.getString(obj, "version");
		if (version != null)
			return Version.fromString(version).getValue();
		else
			return 0;
	}

	static long getLastChange(JsonObject obj) {
		if (obj == null)
			return 0;
		var date = Json.getDate(obj, "lastChange");
		return date == null
				? 0
				: date.getTime();
	}

	static void mapAtts(JsonObject obj, RootEntity entity, long id) {
		if (obj == null || entity == null)
			return;
		entity.id = id;
		entity.name = Json.getString(obj, "name");
		entity.description = Json.getString(obj, "description");
		entity.refId = Json.getString(obj, "@id");
		entity.version = getVersion(obj);
		entity.lastChange = getLastChange(obj);
	}

	static void mapAtts(JsonObject obj, CategorizedEntity entity, long id,
						ImportConfig conf) {
		if (obj == null || entity == null)
			return;
		mapAtts(obj, entity, id);
		var catId = Json.getRefId(obj, "category");
		entity.category = CategoryImport.run(catId, conf);

		// read tags
		var tagArray = Json.getArray(obj, "tags");
		if (tagArray != null) {
			var tags = Json.stream(tagArray)
					.filter(JsonElement::isJsonPrimitive)
					.map(JsonElement::getAsString)
					.filter(tag -> !Strings.nullOrEmpty(tag))
					.toArray(String[]::new);
			entity.tags = tags.length > 0
					? String.join(",", tags)
					: null;
		}
	}

	static boolean isNewer(JsonObject json, RootEntity model) {
		long jsonVersion = getVersion(json);
		long jsonDate = getLastChange(json);
		if (jsonVersion < model.version)
			return false;
		return jsonVersion != model.version || jsonDate > model.lastChange;
	}

}
