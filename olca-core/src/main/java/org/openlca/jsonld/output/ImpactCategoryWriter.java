package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Parameter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record ImpactCategoryWriter(JsonExport exp) implements Writer<ImpactCategory> {

	@Override
	public JsonObject write(ImpactCategory impact) {
		var obj = Writer.init(impact);
		Json.put(obj, "code", impact.code);
		Json.put(obj, "refUnit", impact.referenceUnit);
		Json.put(obj, "source", exp.handleRef(impact.source));
		Json.put(obj, "direction", impact.direction);
		mapImpactFactors(impact, obj);
		mapParameters(obj, impact);
		GlobalParameters.sync(impact, exp);
		return obj;
	}

	private void mapImpactFactors(ImpactCategory impact, JsonObject json) {
		if (impact.impactFactors.isEmpty())
			return;
		var array = new JsonArray();
		for (var f : impact.impactFactors) {
			var obj = new JsonObject();
			Json.put(obj, "value", f.value);
			Json.put(obj, "formula", f.formula);
			Json.put(obj, "flow", exp.handleRef(f.flow));
			Json.put(obj, "unit", Json.asRef(f.unit));
			FlowProperty property = null;
			if (f.flowPropertyFactor != null)
				property = f.flowPropertyFactor.flowProperty;
			Json.put(obj, "flowProperty", exp.handleRef(property));
			Json.put(obj, "uncertainty", Uncertainties.map(f.uncertainty));
			Json.put(obj, "location", exp.handleRef(f.location));
			array.add(obj);
		}
		Json.put(json, "impactFactors", array);
	}

	private void mapParameters(JsonObject json, ImpactCategory impact) {
		var parameters = new JsonArray();
		for (Parameter p : impact.parameters) {
			var obj = new JsonObject();
			ParameterWriter.mapAttr(obj, p);
			parameters.add(obj);
		}
		Json.put(json, "parameters", parameters);
	}
}
