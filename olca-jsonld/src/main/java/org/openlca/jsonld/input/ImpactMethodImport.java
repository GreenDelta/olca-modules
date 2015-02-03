package org.openlca.jsonld.input;

import java.util.Objects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactMethodImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private String refId;
	private EntityStore store;
	private Db db;

	private ImpactMethodImport(String refId, EntityStore store, Db db) {
		this.refId = refId;
		this.store = store;
		this.db = db;
	}

	static ImpactMethod run(String refId, EntityStore store, Db db) {
		return new ImpactMethodImport(refId, store, db).run();
	}

	private ImpactMethod run() {
		if (refId == null || store == null || db == null)
			return null;
		try {
			ImpactMethod m = db.getMethod(refId);
			if (m != null)
				return m;
			JsonObject json = store.get(ModelType.IMPACT_METHOD, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import impact method " + refId, e);
			return null;
		}
	}

	private ImpactMethod map(JsonObject json) {
		if (json == null)
			return null;
		ImpactMethod m = new ImpactMethod();
		In.mapAtts(json, m);
		String catId = In.getRefId(json, "category");
		m.setCategory(CategoryImport.run(catId, store, db));
		mapCategories(json, m);
		return db.put(m);
	}

	private void mapCategories(JsonObject json, ImpactMethod m) {
		JsonElement elem = json.get("impactCategories");
		if (elem == null || !elem.isJsonArray())
			return;
		for (JsonElement e : elem.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			String catId = In.getString(e.getAsJsonObject(), "@id");
			JsonObject catJson = store.get(ModelType.IMPACT_CATEGORY, catId);
			ImpactCategory category = mapCategory(catJson);
			if (category != null)
				m.getImpactCategories().add(category);
		}
	}

	private ImpactCategory mapCategory(JsonObject json) {
		if (json == null)
			return null;
		ImpactCategory cat = new ImpactCategory();
		In.mapAtts(json, cat);
		cat.setReferenceUnit(In.getString(json, "referenceUnit"));
		JsonElement factorsElem = json.get("impactFactors");
		if (factorsElem == null || !factorsElem.isJsonArray())
			return cat;
		for (JsonElement e : factorsElem.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			ImpactFactor factor = mapFactor(e.getAsJsonObject());
			cat.getImpactFactors().add(factor);
		}
		return cat;
	}

	private ImpactFactor mapFactor(JsonObject json) {
		ImpactFactor factor = new ImpactFactor();
		factor.setValue(In.getDouble(json, "value", 0));
		Flow flow = FlowImport.run(In.getRefId(json, "flow"), store, db);
		factor.setFlow(flow);
		Unit unit = db.getUnit(In.getRefId(json, "unit"));
		factor.setUnit(unit);
		FlowPropertyFactor propFac = getPropertyFactor(json, flow);
		factor.setFlowPropertyFactor(propFac);
		// TODO: uncertainty
		// TODO: formula
		return factor;
	}

	private FlowPropertyFactor getPropertyFactor(JsonObject json, Flow flow) {
		JsonElement elem = json.get("flowPropertyFactor");
		if (elem == null || !elem.isJsonObject())
			return null;
		String propId = In.getRefId(elem.getAsJsonObject(), "flowProperty");
		for (FlowPropertyFactor fac : flow.getFlowPropertyFactors()) {
			FlowProperty prop = fac.getFlowProperty();
			if (prop == null)
				continue;
			if (Objects.equals(propId, prop.getRefId()))
				return fac;
		}
		return null;
	}
}
