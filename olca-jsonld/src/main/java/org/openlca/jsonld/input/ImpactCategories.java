package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Unit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ImpactCategories {

	static ImpactCategory map(JsonObject json, ImportConfig conf) {
		if (json == null)
			return null;
		ImpactCategory cat = new ImpactCategory();
		In.mapAtts(json, cat);
		cat.setReferenceUnit(In.getString(json, "referenceUnitName"));
		JsonElement factorsElem = json.get("impactFactors");
		if (factorsElem == null || !factorsElem.isJsonArray())
			return cat;
		for (JsonElement e : factorsElem.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			ImpactFactor factor = mapFactor(e.getAsJsonObject(), conf);
			cat.getImpactFactors().add(factor);
		}
		return cat;
	}

	private static ImpactFactor mapFactor(JsonObject json, ImportConfig conf) {
		ImpactFactor factor = new ImpactFactor();
		factor.setValue(In.getDouble(json, "value", 0));
		factor.setFormula(In.getString(json, "formula"));
		Flow flow = FlowImport.run(In.getRefId(json, "flow"), conf);
		factor.setFlow(flow);
		Unit unit = conf.db.getUnit(In.getRefId(json, "unit"));
		factor.setUnit(unit);
		FlowPropertyFactor propFac = getPropertyFactor(json, flow);
		factor.setFlowPropertyFactor(propFac);
		JsonElement u = json.get("uncertainty");
		if (u != null && u.isJsonObject())
			factor.setUncertainty(Uncertainties.read(u.getAsJsonObject()));
		return factor;
	}

	private static FlowPropertyFactor getPropertyFactor(JsonObject json,
			Flow flow) {
		String propId = In.getRefId(json, "flowProperty");
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
