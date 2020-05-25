package org.openlca.jsonld.input;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class FlowPropertyFactorImport extends BaseEmbeddedImport<FlowPropertyFactor, Flow> {

	private FlowPropertyFactorImport(String flowRefId, ImportConfig conf) {
		super(ModelType.FLOW, flowRefId, conf);
	}

	static FlowPropertyFactor run(String flowRefId, JsonObject json, ImportConfig conf) {
		return new FlowPropertyFactorImport(flowRefId, conf).run(json);
	}

	@Override
	FlowPropertyFactor map(JsonObject json, long id) {
		FlowPropertyFactor f = new FlowPropertyFactor();
		f.id = id;
		String propId = Json.getRefId(json, "flowProperty");
		FlowProperty property = FlowPropertyImport.run(propId, conf);
		f.flowProperty = property;
		f.conversionFactor = Json.getDouble(json, "conversionFactor", 1.0);
		return f;
	}

	@Override
	FlowPropertyFactor getPersisted(Flow flow, JsonObject json) {
		String propertyRefId = Json.getRefId(json, "flowProperty");
		if (propertyRefId == null)
			return null;
		for (FlowPropertyFactor factor : flow.flowPropertyFactors)
			if (factor.flowProperty != null && propertyRefId.equals(factor.flowProperty.refId))
				return factor;
		return null;
	}

}
