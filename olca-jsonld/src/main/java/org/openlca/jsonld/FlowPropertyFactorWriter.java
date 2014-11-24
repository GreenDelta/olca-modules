package org.openlca.jsonld;

import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.openlca.core.model.FlowPropertyFactor;

class FlowPropertyFactorWriter implements JsonSerializer<FlowPropertyFactor> {

	@Override
	public JsonElement serialize(FlowPropertyFactor factor, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(factor, obj);
		return obj;
	}

	static void map(FlowPropertyFactor factor, JsonObject obj) {
		if (factor == null || obj == null)
			return;
		obj.addProperty("@type", "FlowPropertyFactor");
		if (factor.getId() != 0)
			obj.addProperty("@id", factor.getId());
		obj.add("flowProperty", JsonWriter.createRef(factor.getFlowProperty()));
		obj.addProperty("value", factor.getConversionFactor());
	}
}
