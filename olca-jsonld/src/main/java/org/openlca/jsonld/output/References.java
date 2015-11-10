package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class References {

	static JsonObject create(RootEntity ref) {
		if (ref == null)
			return null;
		JsonObject obj = new JsonObject();
		String type = ref.getClass().getSimpleName();
		obj.addProperty("@type", type);
		obj.addProperty("@id", ref.getRefId());
		obj.addProperty("name", ref.getName());
		return obj;
	}

	static JsonObject create(RootEntity ref, Consumer<RootEntity> handler) {
		JsonObject obj = create(ref);
		if (obj == null)
			return null;
		handler.accept(ref);
		return obj;
	}
	
}
