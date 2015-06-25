package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private ImportConfig conf;

	private FlowImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static Flow run(String refId, ImportConfig conf) {
		return new FlowImport(refId, conf).run();
	}

	private Flow run() {
		if (refId == null || conf == null)
			return null;
		try {
			Flow f = conf.db.getFlow(refId);
			if (f != null && !conf.updateExisting)
				return f;
			JsonObject json = conf.store.get(ModelType.FLOW, refId);
			if(f == null)
				return createFlow(json);
			else
				return checkUpdate(json, f);
		} catch (Exception e) {
			log.error("failed to import flow " + refId, e);
			return null;
		}
	}

	private Flow createFlow(JsonObject json) {
		if (json == null)
			return null;
		Flow flow = new Flow();
		mapFlowAtts(json, flow);
		addFactors(json, flow);
		flow = conf.db.put(flow);
		return flow;
	}

	private Flow checkUpdate(JsonObject json, Flow flow) {
		long jsonVersion = In.getVersion(json);
		long jsonDate = In.getLastChange(json);
		if(jsonVersion < flow.getVersion())
			return flow;
		if(jsonVersion == flow.getVersion() && jsonDate <= flow.getLastChange())
			return flow;
		// newer version or same version with newer date
		mapFlowAtts(json, flow);
		return conf.db.update(flow);
	}

	private void mapFlowAtts(JsonObject json, Flow flow) {
		In.mapAtts(json, flow);
		String catId = In.getRefId(json, "category");
		flow.setCategory(CategoryImport.run(catId, conf));
		String typeString = In.getString(json, "flowType");
		if (typeString != null)
			flow.setFlowType(FlowType.valueOf(typeString));
		flow.setCasNumber(In.getString(json, "cas"));
		flow.setFormula(In.getString(json, "formula"));
		String locId = In.getRefId(json, "location");
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
			JsonObject facObj = e.getAsJsonObject();
			FlowPropertyFactor fac = new FlowPropertyFactor();
			flow.getFlowPropertyFactors().add(fac);
			String propId = In.getRefId(facObj, "flowProperty");
			FlowProperty property = FlowPropertyImport.run(propId, conf);
			fac.setFlowProperty(property);
			boolean isRef = In.getBool(facObj, "referenceFlowProperty", false);
			if (isRef)
				flow.setReferenceFlowProperty(property);
			fac.setConversionFactor(In.getDouble(facObj, "conversionFactor", 1.0));
		}
	}
}
