package org.openlca.jsonld.input;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Dates;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

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
		String lastChange = Json.getString(obj, "lastChange");
		if (lastChange != null)
			return Dates.getTime(lastChange);
		else
			return 0;
	}

	static void mapAtts(JsonObject obj, RootEntity entity, long id) {
		if (obj == null || entity == null)
			return;
		entity.setId(id);
		entity.setName(Json.getString(obj, "name"));
		entity.setDescription(Json.getString(obj, "description"));
		entity.setRefId(Json.getString(obj, "@id"));
		entity.setVersion(getVersion(obj));
		entity.setLastChange(getLastChange(obj));
	}

	static void mapAtts(JsonObject obj, CategorizedEntity entity, long id,
			ImportConfig conf) {
		if (obj == null || entity == null)
			return;
		mapAtts(obj, (RootEntity) entity, id);
		String catId = Json.getRefId(obj, "category");
		entity.setCategory(CategoryImport.run(catId, conf));
	}
	
	static boolean isNewer(JsonObject json, RootEntity model) {
		long jsonVersion = getVersion(json);
		long jsonDate = getLastChange(json);
		if (jsonVersion < model.getVersion())
			return false;
		if (jsonVersion == model.getVersion() && jsonDate <= model.getLastChange())
			return false;
		return true;
	}

}
