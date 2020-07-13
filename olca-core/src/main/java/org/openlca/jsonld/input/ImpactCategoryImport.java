package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ImpactCategoryImport extends BaseImport<ImpactCategory> {

	private ImpactCategoryImport(String refID, ImportConfig conf) {
		super(ModelType.IMPACT_CATEGORY, refID, conf);
	}

	static ImpactCategory run (String refID, ImportConfig conf) {
		return new ImpactCategoryImport(refID, conf).run();
	}

	@Override
	ImpactCategory map(JsonObject json, long id) {
		if (json == null)
			return null;
		ImpactCategory cat = new ImpactCategory();
		In.mapAtts(json, cat, id, conf);
		cat.referenceUnit = Json.getString(json, "referenceUnitName");
		mapParameters(json, cat);
		JsonArray factors = Json.getArray(json, "impactFactors");
		if (factors != null) {
			for (var e : factors) {
				if (!e.isJsonObject())
					continue;
				var factor = mapFactor(e.getAsJsonObject(), conf);
				if (factor == null)
					continue;
				cat.impactFactors.add(factor);
			}
		}
		return conf.db.put(cat);
	}

	private ImpactFactor mapFactor(JsonObject json, ImportConfig conf) {
		if (json == null || conf == null)
			return null;

		ImpactFactor factor = new ImpactFactor();
		factor.value = Json.getDouble(json, "value", 0);
		factor.formula = Json.getString(json, "formula");
		String flowId = Json.getRefId(json, "flow");
		Flow flow = FlowImport.run(flowId, conf);
		factor.flow = flow;
		if (flow == null) {
			conf.log.warn("invalid flow {}; LCIA factor not imported", flowId);
			return null;
		}

		JsonElement uncertainty = json.get("uncertainty");
		if (uncertainty != null && uncertainty.isJsonObject()) {
			factor.uncertainty = Uncertainties.read(
					uncertainty.getAsJsonObject());
		}

		// set the flow property and unit; if we cannot find them
		// we will choose the reference data from the flow
		// when we cannot find consistent information we return
		// a factor where the unit or flow property factor may
		// is absent.
		Unit unit = conf.db.get(ModelType.UNIT, Json.getRefId(json, "unit"));
		FlowPropertyFactor propFac = getPropertyFactor(json, flow);
		if (unit != null && propFac != null) {
			factor.unit = unit;
			factor.flowPropertyFactor = propFac;
			return factor;
		}

		if (propFac == null) {
			propFac = flow.getReferenceFactor();
			if (propFac == null || propFac.flowProperty == null)
				return factor;
		}
		factor.flowPropertyFactor = propFac;

		UnitGroup ug = propFac.flowProperty.unitGroup;
		if (ug == null)
			return factor;

		if (unit == null) {
			factor.unit = ug.referenceUnit;
		} else {
			for (Unit u : ug.units) {
				if (Objects.equals(u, unit)) {
					factor.unit = u;
				}
			}
		}
		return factor;
	}

	private FlowPropertyFactor getPropertyFactor(JsonObject json,
			Flow flow) {
		if (json == null || flow == null)
			return null;
		String propId = Json.getRefId(json, "flowProperty");
		for (FlowPropertyFactor fac : flow.flowPropertyFactors) {
			FlowProperty prop = fac.flowProperty;
			if (prop == null)
				continue;
			if (Objects.equals(propId, prop.refId))
				return fac;
		}
		return null;
	}

	private void mapParameters(JsonObject json, ImpactCategory impact) {
		var parameters = Json.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (var e : parameters) {
			if (!e.isJsonObject())
				continue;
			var o = e.getAsJsonObject();
			var parameter = new Parameter();
			ParameterImport.mapFields(o, parameter);
			impact.parameters.add(parameter);
		}
	}
}
