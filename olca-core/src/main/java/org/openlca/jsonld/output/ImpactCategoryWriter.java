package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Parameter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ImpactCategoryWriter extends Writer<ImpactCategory> {

	ImpactCategoryWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(ImpactCategory impact) {
		JsonObject obj = super.write(impact);
		if (obj == null)
			return null;
		Out.put(obj, "referenceUnitName", impact.referenceUnit);
		Out.put(obj, "source", impact.source, conf);
		mapImpactFactors(impact, obj);
		mapParameters(obj, impact);
		GlobalParameters.sync(impact, conf);
		return obj;
	}

	private void mapImpactFactors(ImpactCategory impact, JsonObject json) {
		if (impact.impactFactors.isEmpty())
			return;
		var array = new JsonArray();
		for (var f : impact.impactFactors) {
			var obj = new JsonObject();
			Out.put(obj, "@type", ImpactFactor.class.getSimpleName());
			Out.put(obj, "value", f.value);
			Out.put(obj, "formula", f.formula);
			Out.put(obj, "flow", f.flow, conf, Out.REQUIRED_FIELD);
			if (f.flow != null) {
				var flow = obj.get("flow").getAsJsonObject();
				Out.put(flow, "flowType", f.flow.flowType);
			}
			Out.put(obj, "unit", f.unit, conf, Out.REQUIRED_FIELD);
			FlowProperty property = null;
			if (f.flowPropertyFactor != null)
				property = f.flowPropertyFactor.flowProperty;
			Out.put(obj, "flowProperty", property, conf, Out.REQUIRED_FIELD);
			Out.put(obj, "uncertainty", Uncertainties.map(f.uncertainty));
			Out.put(obj, "location", f.location, conf);
			array.add(obj);
		}
		Out.put(json, "impactFactors", array);
	}

	private void mapParameters(JsonObject json, ImpactCategory impact) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : impact.parameters) {
			JsonObject obj = Writer.initJson();
			ParameterWriter.mapAttr(obj, p);
			parameters.add(obj);
		}
		Out.put(json, "parameters", parameters);
	}
}
