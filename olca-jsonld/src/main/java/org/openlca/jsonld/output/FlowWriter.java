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
		Out.put(obj, "flowType", flow.getFlowType());
		Out.put(obj, "cas", flow.getCasNumber());
		Out.put(obj, "formula", flow.getFormula());
		Out.put(obj, "location", flow.getLocation(), refFn);
		addFactors(flow, obj, refFn);
		return obj;
	}

	private void addFactors(Flow flow, JsonObject obj,
			Consumer<RootEntity> refFn) {
		JsonArray factorArray = new JsonArray();
		for (FlowPropertyFactor fac : flow.getFlowPropertyFactors()) {
			JsonObject facObj = new JsonObject();
			Out.put(facObj, "@type", "FlowPropertyFactor");
			if (Objects.equals(fac, flow.getReferenceFactor()))
				Out.put(facObj, "referenceFlowProperty", true);
			Out.put(facObj, "flowProperty", fac.getFlowProperty(), refFn);
			Out.put(facObj, "conversionFactor", fac.getConversionFactor());
			factorArray.add(facObj);
		}
		Out.put(obj, "flowProperties", factorArray);
	}

}
