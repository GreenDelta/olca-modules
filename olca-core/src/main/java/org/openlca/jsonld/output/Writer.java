package org.openlca.jsonld.output;

import java.time.Instant;
import java.util.Arrays;

import com.google.gson.JsonArray;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Version;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class Writer<T extends RefEntity> {

	final JsonExport exp;

	protected Writer(JsonExport exp) {
		this.exp = exp;
	}

	JsonObject write(T entity) {
		var obj = new JsonObject();
		// mark entity directly as visited to avoid endless cyclic exports for
		// cyclic references
		if (entity instanceof RootEntity ce) {
			exp.setVisited(ce);
		}
		mapBasicAttributes(entity, obj);
		return obj;
	}

	static void mapBasicAttributes(RefEntity entity, JsonObject obj) {
		if (entity == null || obj == null)
			return;
		var type = entity.getClass().getSimpleName();
		Json.put(obj, "@type", type);
		Json.put(obj, "@id", entity.refId);
		Json.put(obj, "name", entity.name);
		Json.put(obj, "description", entity.description);
		if (entity instanceof RootEntity re) {

			if (re.category != null) {
				Json.put(obj, "category", re.category.toPath());
			}
			Json.put(obj, "library", re.library);
			Json.put(obj, "version", Version.asString(re.version));
			if (re.lastChange != 0) {
				var instant = Instant.ofEpochMilli(re.lastChange);
				Json.put(obj, "lastChange", instant.toString());
			}

			// tags
			if (!Strings.nullOrEmpty(re.tags)) {
				var tags = new JsonArray();
				Arrays.stream(re.tags.split(","))
					.map(String::trim)
					.filter(tag -> !Strings.nullOrEmpty(tag))
					.forEach(tags::add);
				if (tags.size() > 0) {
					obj.add("tags", tags);
				}
			}
		}
	}

	boolean isExportExternalFiles() {
		return true;
	}

}
