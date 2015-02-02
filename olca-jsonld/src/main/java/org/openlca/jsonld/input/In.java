package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.RootEntity;

final class In {

	private In() {
	}

	static String getString(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
	}

	/**
	 * Returns the ID of a referenced entity (see Out.writeRef).
	 */
	static String getRefId(JsonObject obj, String refName) {
		if (obj == null || refName == null)
			return null;
		JsonElement elem = obj.get(refName);
		if (elem == null || !elem.isJsonObject())
			return null;
		return getString(elem.getAsJsonObject(), "@id");
	}

	static void mapAtts(JsonObject obj, RootEntity entity) {
		if (obj == null || entity == null)
			return;
		entity.setName(getString(obj, "name"));
		entity.setDescription(getString(obj, "description"));
		entity.setRefId(getString(obj, "@id"));
	}

}
