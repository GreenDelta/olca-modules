package org.openlca.ipc.handlers;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class Models {

	/**
	 * Returns a descriptor with the model type and (reference ID) from the given
	 * Json object. It returns only a descriptor when both parameters (ID and type)
	 * can be set. Otherwise it returns null.
	 */
	static Descriptor getDescriptor(JsonObject obj) {
		if (obj == null)
			return null;
		String id = Json.getString(obj, "@id");
		ModelType type = getType(obj);
		if (id == null || type == null)
			return null;
		Descriptor d = new Descriptor();
		d.refId = id;
		d.type = type;
		d.name = Json.getString(obj, "name");
		return d;
	}

	static ModelType getType(JsonObject obj) {
		if (obj == null)
			return null;
		String typeStr = Json.getString(obj, "@type");
		if (typeStr == null)
			return null;
		try {
			Class<?> clazz = Class.forName("org.openlca.core.model." + typeStr);
			return ModelType.forModelClass(clazz);
		} catch (Exception e) {
			return null;
		}
	}
}
