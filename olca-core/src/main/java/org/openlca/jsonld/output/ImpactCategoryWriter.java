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
	JsonObject write(ImpactCategory category) {
		JsonObject obj = super.write(category);
		if (obj == null)
			return null;
		Out.put(obj, "referenceUnitName", category.referenceUnit);
		mapImpactFactors(category, obj);
		mapParameters(obj, category);
		GlobalParameters.sync(category, conf);
		return obj;
	}

	private void mapImpactFactors(ImpactCategory category, JsonObject json) {
		if (conf.isLibraryExport)
			return;
		JsonArray array = new JsonArray();
		for (ImpactFactor f : category.impactFactors) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", ImpactFactor.class.getSimpleName());
			Out.put(obj, "value", f.value);
			Out.put(obj, "formula", f.formula);
			Out.put(obj, "flow", f.flow, conf, Out.REQUIRED_FIELD);
			if (f.flow != null) {
				JsonObject flow = obj.get("flow").getAsJsonObject();
				Out.put(flow, "flowType", f.flow.flowType);
			}
			Out.put(obj, "unit", f.unit, conf, Out.REQUIRED_FIELD);
			FlowProperty property = null;
			if (f.flowPropertyFactor != null)
				property = f.flowPropertyFactor.flowProperty;
			Out.put(obj, "flowProperty", property, conf, Out.REQUIRED_FIELD);
			Out.put(obj, "uncertainty", Uncertainties.map(f.uncertainty));
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
