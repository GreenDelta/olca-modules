package org.openlca.ipc;

import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;

class Models {

	/**
	 * Returns a descriptor with the model type and (reference ID) from the
	 * given Json object. It returns only a descriptor when both parameters
	 * (ID and type)  can be set. Otherwise it returns null.
	 */
	static BaseDescriptor getDescriptor(JsonObject obj) {
		if (obj == null)
			return null;
		String id = Json.getString(obj, "@id");
		ModelType type = getType(obj);
		if (id == null || type == null)
			return null;
		BaseDescriptor d = new BaseDescriptor();
		d.setRefId(id);
		d.setType(type);
		d.setName(Json.getString(obj, "name"));
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
