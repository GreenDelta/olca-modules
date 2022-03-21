package org.openlca.jsonld.input;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class FlowImport extends BaseImport<Flow> {

	private FlowImport(String refId, JsonImport conf) {
		super(ModelType.FLOW, refId, conf);
	}

	static Flow run(String refId, JsonImport conf) {
		return new FlowImport(refId, conf).run();
	}

	@Override
	Flow map(JsonObject json, long id) {
		if (json == null)
			return null;
		Flow flow = new Flow();
		In.mapAtts(json, flow, id, conf);
		mapFlowAtts(json, flow);
		addFactors(json, flow);
		return conf.db.put(flow);
	}

	private void mapFlowAtts(JsonObject json, Flow flow) {
		flow.flowType = Json.getEnum(json, "flowType", FlowType.class);
		flow.casNumber = Json.getString(json, "cas");
		flow.synonyms = Json.getString(json, "synonyms");
		flow.formula = Json.getString(json, "formula");
		flow.infrastructureFlow = Json.getBool(json, "isInfrastructureFlow", false);
		String locId = Json.getRefId(json, "location");
		if (locId != null)
			flow.location = LocationImport.run(locId, conf);
	}

	private void addFactors(JsonObject json, Flow flow) {
		var array = Json.getArray(json, "flowProperties");
		if (array == null)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var factor = FlowPropertyFactorImport.run(flow.refId, obj, conf);
			flow.flowPropertyFactors.add(factor);
			boolean isRef = Json.getBool(obj, "isRefFlowProperty", false);
			if (isRef)
				flow.referenceFlowProperty = factor.flowProperty;
		}
	}
}
