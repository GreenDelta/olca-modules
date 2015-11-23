package org.openlca.jsonld.output;

import java.lang.reflect.Type;

import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class DescriptorWriter implements JsonSerializer<BaseDescriptor> {

	@Override
	public JsonElement serialize(BaseDescriptor descriptor, Type type,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		if (descriptor == null || descriptor.getModelType() == null)
			return json;
		addContext(json);
		String clazz = descriptor.getModelType().getModelClass()
				.getSimpleName();
		Out.put(json, "@type", clazz);
		Out.put(json, "@id", descriptor.getRefId());
		Out.put(json, "name", descriptor.getName());
		return json;
	}

	private void addContext(JsonObject json) {
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", "http://openlca.org/");
		Out.put(json, "@context", context);
	}

}
