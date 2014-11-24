package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class DescriptorWriter implements JsonSerializer<BaseDescriptor> {

	public DescriptorWriter(JsonWriter writer) {
	}

	@Override
	public JsonElement serialize(BaseDescriptor descriptor, Type type,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (descriptor == null || descriptor.getModelType() == null)
			return json;
		addContext(json);
		String clazz = descriptor.getModelType().getModelClass()
				.getSimpleName();
		json.addProperty("@type", clazz);
		json.addProperty("@id", descriptor.getRefId());
		json.addProperty("name", descriptor.getName());
		return json;
	}

	private void addContext(JsonObject json) {
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", "http://openlca.org/");
		json.add("@context", context);
	}

}
