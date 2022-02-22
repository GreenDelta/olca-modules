package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class FlowWriter extends Writer<Flow> {

	FlowWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	public JsonObject write(Flow flow) {
		JsonObject obj = super.write(flow);
		if (obj == null)
			return null;
		Out.put(obj, "flowType", flow.flowType);
		Out.put(obj, "cas", flow.casNumber);
		Out.put(obj, "formula", flow.formula);
		Out.put(obj, "synonyms", flow.synonyms);
		Out.put(obj, "infrastructureFlow", flow.infrastructureFlow);
		Out.put(obj, "location", flow.location, conf);
		addFactors(flow, obj);
		return obj;
	}

	private void addFactors(Flow flow, JsonObject obj) {
		JsonArray factorArray = new JsonArray();
		for (FlowPropertyFactor fac : flow.flowPropertyFactors) {
			JsonObject facObj = new JsonObject();
			Out.put(facObj, "@type", FlowPropertyFactor.class.getSimpleName());
			if (Objects.equals(fac, flow.getReferenceFactor()))
				Out.put(facObj, "referenceFlowProperty", true);
			Out.put(facObj, "flowProperty", fac.flowProperty, conf);
			Out.put(facObj, "conversionFactor", fac.conversionFactor);
			factorArray.add(facObj);
		}
		Out.put(obj, "flowProperties", factorArray);
	}

}
