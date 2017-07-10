package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;

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
		return obj;
	}

	private void mapImpactFactors(ImpactCategory category, JsonObject json) {
		JsonArray array = new JsonArray();
		for (ImpactFactor f : category.impactFactors) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", ImpactFactor.class.getSimpleName());
			Out.put(obj, "value", f.value);
			Out.put(obj, "formula", f.formula);
			Out.put(obj, "flow", f.flow, conf, Out.REQUIRED_FIELD);
			Out.put(obj, "unit", f.unit, conf, Out.REQUIRED_FIELD);
			FlowProperty property = null;
			if (f.flowPropertyFactor != null)
				property = f.flowPropertyFactor.getFlowProperty();
			Out.put(obj, "flowProperty", property, conf, Out.REQUIRED_FIELD);
			Out.put(obj, "uncertainty", Uncertainties.map(f.uncertainty));
			array.add(obj);
		}
		Out.put(json, "impactFactors", array);
	}
}
