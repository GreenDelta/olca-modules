package org.openlca.jsonld.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class Util {

	private Util() {
	}

	static void mapBase(RefEntity e, JsonObject obj, EntityResolver resolver) {

		e.refId = Json.getString(obj, "@id");
		e.name = Json.getString(obj, "name");
		e.description = Json.getString(obj, "description");

		if (!(e instanceof RootEntity re))
			return;

		// version & last change
		re.version = getVersion(obj);
		re.lastChange = getLastChange(obj);

		// category
		var path = Json.getString(obj, "category");
		var type = ModelType.of(re);
		re.category = resolver.getCategory(type, path);

		// tags
		var tags = Json.getStrings(Json.getArray(obj, "tags"));
		re.tags = tags.length > 0
				? String.join(",", tags)
				: null;
	}

	static long getVersion(JsonObject obj) {
		if (obj == null)
			return 0;
		String version = Json.getString(obj, "version");
		if (version != null)
			return Version.fromString(version).getValue();
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
}
