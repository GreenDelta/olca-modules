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
		Out.put(obj, "referenceUnitName", category.getReferenceUnit());
		mapImpactFactors(category, obj);
		return obj;
	}

	private void mapImpactFactors(ImpactCategory category, JsonObject json) {
		JsonArray array = new JsonArray();
		for (ImpactFactor f : category.getImpactFactors()) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", ImpactFactor.class.getSimpleName());
			Out.put(obj, "value", f.getValue());
			Out.put(obj, "formula", f.getFormula());
			Out.put(obj, "flow", f.getFlow(), conf, Out.REQUIRED_FIELD);
			Out.put(obj, "unit", f.getUnit(), conf, Out.REQUIRED_FIELD);
			FlowProperty property = null;
			if (f.getFlowPropertyFactor() != null)
				property = f.getFlowPropertyFactor().getFlowProperty();
			Out.put(obj, "flowProperty", property, conf, Out.REQUIRED_FIELD);
			Out.put(obj, "uncertainty", Uncertainties.map(f.getUncertainty()));
			array.add(obj);
		}
		Out.put(json, "impactFactors", array);
	}
}
