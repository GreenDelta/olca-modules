package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class FlowWriter extends Writer<Flow> {

	FlowWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	public JsonObject write(Flow flow) {
		JsonObject obj = super.write(flow);
		if (obj == null)
			return null;
		Json.put(obj, "flowType", flow.flowType);
		Json.put(obj, "cas", flow.casNumber);
		Json.put(obj, "formula", flow.formula);
		Json.put(obj, "synonyms", flow.synonyms);
		Json.put(obj, "isInfrastructureFlow", flow.infrastructureFlow);
		Json.put(obj, "location", exp.handleRef(flow.location));
		addFactors(flow, obj);
		return obj;
	}

	private void addFactors(Flow flow, JsonObject obj) {
		var array = new JsonArray();
		for (var factor : flow.flowPropertyFactors) {
			var facObj = new JsonObject();
			Json.put(facObj, "@type", FlowPropertyFactor.class.getSimpleName());
			if (Objects.equals(factor, flow.getReferenceFactor())) {
				Json.put(facObj, "referenceFlowProperty", true);
			}
			Json.put(facObj, "flowProperty", exp.handleRef(factor.flowProperty));
			Json.put(facObj, "conversionFactor", factor.conversionFactor);
			array.add(facObj);
		}
		Json.put(obj, "flowProperties", array);
	}

}
