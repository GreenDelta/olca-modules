package org.openlca.core.json;

import java.lang.reflect.Type;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class FlowWriter implements JsonSerializer<Flow> {

	@Override
	public JsonElement serialize(Flow flow, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(flow, obj);
		return obj;
	}

	static void map(Flow flow, JsonObject obj) {
		if (flow == null || obj == null)
			return;
		JsonWriter.addAttributes(flow, obj);
		if (flow.getFlowType() != null)
			obj.addProperty("flowType", flow.getFlowType().name());
		obj.addProperty("cas", flow.getCasNumber());
		obj.addProperty("formula", flow.getFormula());
		JsonObject locationRef = JsonWriter.createRef(flow.getLocation());
		obj.add("location", locationRef);
		JsonObject propRef = JsonWriter.createRef(
				flow.getReferenceFlowProperty());
		obj.add("referenceFlowProperty", propRef);
		addFactors(flow, obj);
	}

	private static void addFactors(Flow flow, JsonObject obj) {
		JsonArray factorArray = new JsonArray();
		for (FlowPropertyFactor factor : flow.getFlowPropertyFactors()) {
			JsonObject factorObj = new JsonObject();
			JsonObject propRef = JsonWriter.createRef(factor
					.getFlowProperty());
			factorObj.addProperty("@type", "FlowPropertyFactor");
			factorObj.add("flowProperty", propRef);
			factorObj.addProperty("value", factor.getConversionFactor());
			factorArray.add(factorObj);
		}
		obj.add("flowPropertyFactors", factorArray);
	}

}
