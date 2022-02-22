package org.openlca.jsonld.input;

import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ImpactCategoryImport extends BaseImport<ImpactCategory> {

	private ImpactCategoryImport(String refID, JsonImport conf) {
		super(ModelType.IMPACT_CATEGORY, refID, conf);
	}

	static ImpactCategory run(String refID, JsonImport conf) {
		return new ImpactCategoryImport(refID, conf).run();
	}

	@Override
	ImpactCategory map(JsonObject json, long id) {
		if (json == null)
			return null;
		var impact = new ImpactCategory();
		In.mapAtts(json, impact, id, conf);
		impact.referenceUnit = Json.getString(json, "referenceUnitName");
		var sourceId = Json.getString(json, "source");
		if (Strings.notEmpty(sourceId)) {
			impact.source = SourceImport.run(sourceId, conf);
		}
		mapParameters(json, impact);
		JsonArray factors = Json.getArray(json, "impactFactors");
		if (factors != null) {
			for (var e : factors) {
				if (!e.isJsonObject())
					continue;
				var factor = mapFactor(e.getAsJsonObject(), conf);
				if (factor == null)
					continue;
				impact.impactFactors.add(factor);
			}
		}
		return conf.db.put(impact);
	}

	private ImpactFactor mapFactor(JsonObject json, JsonImport conf) {
		if (json == null || conf == null)
			return null;

		ImpactFactor factor = new ImpactFactor();

		// flow
		String flowId = Json.getRefId(json, "flow");
		Flow flow = FlowImport.run(flowId, conf);
		factor.flow = flow;
		if (flow == null) {
			// TODO: log this error
			return null;
		}
		var quantity = Quantity.of(flow, json);
		factor.flowPropertyFactor = quantity.factor();
		factor.unit = quantity.unit();

		// amount fields
		factor.value = Json.getDouble(json, "value", 0);
		factor.formula = Json.getString(json, "formula");
		JsonElement uncertainty = json.get("uncertainty");
		if (uncertainty != null && uncertainty.isJsonObject()) {
			factor.uncertainty = Uncertainties.read(
				uncertainty.getAsJsonObject());
		}

		// location
		var locID = Json.getRefId(json, "location");
		if (Strings.notEmpty(locID)) {
			factor.location = LocationImport.run(locID, conf);
		}
		return factor;
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
			parameter.scope = ParameterScope.IMPACT;
			impact.parameters.add(parameter);
		}
	}
}
