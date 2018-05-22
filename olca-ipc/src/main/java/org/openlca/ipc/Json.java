package org.openlca.ipc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.descriptors.BaseDescriptor;

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

	static void put(JsonObject obj, String prop, String val) {
		if (obj == null || val == null)
			return;
		obj.addProperty(prop, val);
	}


	static JsonObject toJson(BaseDescriptor d) {
		if (d == null)
			return null;
		JsonObject obj = new JsonObject();
		if (d.getModelType() != null) {
			String type = d.getModelType().getModelClass().getSimpleName();
			put(obj, "@type", type);
		}
		put(obj, "@id", d.getRefId());
		put(obj, "name", d.getName());
		put(obj, "description", d.getDescription());
		return obj;
	}

}
