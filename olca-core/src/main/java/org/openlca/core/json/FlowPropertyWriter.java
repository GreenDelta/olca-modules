package org.openlca.core.json;

import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;

class FlowPropertyWriter implements JsonSerializer<FlowProperty> {

	@Override
	public JsonElement serialize(FlowProperty property, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(property, obj);
		return obj;
	}

	static void map(FlowProperty property, JsonObject obj) {
		if (property == null || obj == null)
			return;
		JsonWriter.addAttributes(property, obj);
		mapType(property, obj);
		JsonObject unitGroup = JsonWriter.createReference(
				property.getUnitGroup());
		obj.add("unitGroup", unitGroup);
	}

	private static void mapType(FlowProperty property, JsonObject obj) {
		FlowPropertyType type = property.getFlowPropertyType();
		if (type == null)
			return;
		switch (type) {
		case ECONOMIC:
			obj.addProperty("flowPropertyType", "ECONOMIC_QUANTITY");
			break;
		case PHYSICAL:
			obj.addProperty("flowPropertyType", "PHYSICAL_QUANTITY");
			break;
		}
	}

}
