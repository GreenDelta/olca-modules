package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private EntityStore store;
	private Db db;

	private FlowImport(String refId, EntityStore store, Db db) {
		this.refId = refId;
		this.store = store;
		this.db = db;
	}

	static Flow run(String refId, EntityStore store, Db db) {
		return new FlowImport(refId, store, db).run();
	}

	private Flow run() {
		if (refId == null || store == null || db == null)
			return null;
		try {
			Flow f = db.getFlow(refId);
			if (f != null)
				return f;
			JsonObject json = store.get(ModelType.FLOW, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import flow " + refId, e);
			return null;
		}
	}

	private Flow map(JsonObject json) {
		if (json == null)
			return null;
		Flow flow = new Flow();
		In.mapAtts(json, flow);
		String catId = In.getRefId(json, "category");
		flow.setCategory(CategoryImport.run(catId, store, db));
		mapFlowAtts(json, flow);
		addFactors(json, flow);
		flow = db.put(flow);
		return flow;
	}

	private void mapFlowAtts(JsonObject json, Flow flow) {
		String typeString = In.getString(json, "flowType");
		if (typeString != null)
			flow.setFlowType(FlowType.valueOf(typeString));
		flow.setCasNumber(In.getString(json, "cas"));
		flow.setFormula(In.getString(json, "formula"));
		String locId = In.getRefId(json, "location");
		if (locId != null)
			flow.setLocation(LocationImport.run(locId, store, db));
		String propId = In.getRefId(json, "referenceFlowProperty");
		flow.setReferenceFlowProperty(FlowPropertyImport.run(propId, store, db));
	}

	private void addFactors(JsonObject json, Flow flow) {
		JsonElement elem = json.get("flowPropertyFactors");
		if (elem == null || !elem.isJsonArray())
			return;
		for (JsonElement e : elem.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			JsonObject facObj = e.getAsJsonObject();
			FlowPropertyFactor fac = new FlowPropertyFactor();
			flow.getFlowPropertyFactors().add(fac);
			String propId = In.getRefId(facObj, "flowProperty");
			fac.setFlowProperty(FlowPropertyImport.run(propId, store, db));
			fac.setConversionFactor(In.getDouble(facObj, "value", 1.0));
		}
	}
}
