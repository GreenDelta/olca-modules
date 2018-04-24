package org.openlca.ipc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Json {

	private Json() {
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

}
