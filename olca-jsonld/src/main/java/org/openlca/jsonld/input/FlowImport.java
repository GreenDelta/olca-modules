package org.openlca.jsonld.input;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class FlowImport extends BaseImport<Flow> {

	private FlowImport(String refId, ImportConfig conf) {
		super(ModelType.FLOW, refId, conf);
	}

	static Flow run(String refId, ImportConfig conf) {
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
		String catId = Json.getRefId(json, "category");
		flow.setCategory(CategoryImport.run(catId, conf));
		flow.setFlowType(Json.getEnum(json, "flowType", FlowType.class));
		flow.setCasNumber(Json.getString(json, "cas"));
		flow.synonyms = Json.getString(json, "synonyms");
		flow.setFormula(Json.getString(json, "formula"));
		flow.setInfrastructureFlow(Json.getBool(json, "infrastructureFlow", false));
		String locId = Json.getRefId(json, "location");
		if (locId != null)
			flow.setLocation(LocationImport.run(locId, conf));
	}

	private void addFactors(JsonObject json, Flow flow) {
		JsonElement elem = json.get("flowProperties");
		if (elem == null || !elem.isJsonArray())
			return;
		for (JsonElement e : elem.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			JsonObject fObj = e.getAsJsonObject();
			FlowPropertyFactor f = FlowPropertyFactorImport.run(flow.getRefId(), fObj, conf);
			flow.getFlowPropertyFactors().add(f);
			boolean isRef = Json.getBool(fObj, "referenceFlowProperty", false);
			if (isRef)
				flow.setReferenceFlowProperty(f.getFlowProperty());
		}
	}
}
