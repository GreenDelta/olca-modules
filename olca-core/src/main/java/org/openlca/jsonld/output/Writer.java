package org.openlca.jsonld.output;

import java.time.Instant;
import java.util.Arrays;

import com.google.gson.JsonArray;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class Writer<T extends RootEntity> {

	final JsonExport exp;

	protected Writer(JsonExport exp) {
		this.exp = exp;
	}

	JsonObject write(T entity) {
		var obj = new JsonObject();
		// mark entity directly as visited to avoid endless cyclic exports for
		// cyclic references
		if (entity instanceof CategorizedEntity ce) {
			exp.setVisited(ce);
		}
		addBasicAttributes(entity, obj);
		if (entity instanceof CategorizedEntity ce) {
			Json.put(obj, "category", exp.handleRef(ce.category));
			Json.put(obj, "library", ce.library);

			// write tags
			if (!Strings.nullOrEmpty(ce.tags)) {
				var tags = new JsonArray();
				Arrays.stream(ce.tags.split(","))
						.map(String::trim)
						.filter(tag -> !Strings.nullOrEmpty(tag))
						.forEach(tags::add);
				if (tags.size() > 0) {
					obj.add("tags", tags);
				}
			}
		}
		return obj;
	}

	static void addBasicAttributes(RootEntity entity, JsonObject obj) {
		if (entity == null || obj == null)
			return;
		var type = entity.getClass().getSimpleName();
		Json.put(obj, "@type", type);
		Json.put(obj, "@id", entity.refId);
		Json.put(obj, "name", entity.name);
		Json.put(obj, "description", entity.description);
		Json.put(obj, "version", Version.asString(entity.version));
		if (entity.lastChange != 0) {
			var instant = Instant.ofEpochMilli(entity.lastChange);
			Json.put(obj, "lastChange", instant.toString());
		}
	}

	boolean isExportExternalFiles() {
		return true;
	}

}
