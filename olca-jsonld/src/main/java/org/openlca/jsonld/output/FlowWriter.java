package org.openlca.jsonld.output;

import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class FlowWriter extends Writer<Flow> {

	@Override
	public JsonObject write(Flow flow, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(flow, refFn);
		if (obj == null)
			return null;
		map(flow, obj, refFn);
		return obj;
	}

	void map(Flow flow, JsonObject obj, Consumer<RootEntity> refFn) {
		if (flow == null || obj == null)
			return;
		if (flow.getFlowType() != null)
			obj.addProperty("flowType", flow.getFlowType().name());
		obj.addProperty("cas", flow.getCasNumber());
		obj.addProperty("formula", flow.getFormula());
		JsonObject locationRef = References.create(flow.getLocation(), refFn);
		obj.add("location", locationRef);
		addFactors(flow, obj, refFn);
	}

	private void addFactors(Flow flow, JsonObject obj, Consumer<RootEntity> refFn) {
		JsonArray factorArray = new JsonArray();
		for (FlowPropertyFactor fac : flow.getFlowPropertyFactors()) {
			JsonObject facObj = new JsonObject();
			facObj.addProperty("@type", "FlowPropertyFactor");
			if (Objects.equals(fac, flow.getReferenceFactor()))
				facObj.addProperty("referenceFlowProperty", true);
			JsonObject propRef = References.create(fac.getFlowProperty(), refFn);
			facObj.add("flowProperty", propRef);
			facObj.addProperty("conversionFactor", fac.getConversionFactor());
			factorArray.add(facObj);
		}
		obj.add("flowProperties", factorArray);
	}

}
