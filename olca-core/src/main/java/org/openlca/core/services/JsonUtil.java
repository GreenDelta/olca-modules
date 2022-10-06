package org.openlca.core.services;

import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.DbRefs;

final class JsonUtil {

	private JsonUtil() {
	}

	static JsonObject toJson(TechFlow techFlow, DbRefs refs) {
		if (techFlow == null)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "provider", refs.asRef(techFlow.provider()));
		Json.put(obj, "flow", refs.asRef(techFlow.flow()));
		return obj;
	}

}
