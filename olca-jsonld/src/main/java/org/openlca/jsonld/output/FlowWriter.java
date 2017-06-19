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
		Out.put(obj, "flowType", flow.getFlowType(), Out.REQUIRED_FIELD);
		Out.put(obj, "cas", flow.getCasNumber());
		Out.put(obj, "formula", flow.getFormula());
		Out.put(obj, "synonyms", flow.synonyms);
		Out.put(obj, "infrastructureFlow", flow.isInfrastructureFlow());
		Out.put(obj, "location", flow.getLocation(), conf);
		addFactors(flow, obj);
		return obj;
	}

	private void addFactors(Flow flow, JsonObject obj) {
		JsonArray factorArray = new JsonArray();
		for (FlowPropertyFactor fac : flow.getFlowPropertyFactors()) {
			JsonObject facObj = new JsonObject();
			Out.put(facObj, "@type", FlowPropertyFactor.class.getSimpleName());
			if (Objects.equals(fac, flow.getReferenceFactor()))
				Out.put(facObj, "referenceFlowProperty", true);
			Out.put(facObj, "flowProperty", fac.getFlowProperty(), conf, Out.REQUIRED_FIELD);
			Out.put(facObj, "conversionFactor", fac.getConversionFactor());
			factorArray.add(facObj);
		}
		Out.put(obj, "flowProperties", factorArray);
	}

}
