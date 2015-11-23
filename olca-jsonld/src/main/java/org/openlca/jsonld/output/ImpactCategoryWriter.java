package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ImpactCategoryWriter extends Writer<ImpactCategory> {

	@Override
	JsonObject write(ImpactCategory category, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(category, refFn);
		if (obj == null)
			return null;
		Out.put(obj, "referenceUnitName", category.getReferenceUnit());
		mapImpactFactors(category, obj, refFn);
		return obj;
	}

	private void mapImpactFactors(ImpactCategory category, JsonObject json,
			Consumer<RootEntity> refFn) {
		JsonArray array = new JsonArray();
		for (ImpactFactor f : category.getImpactFactors()) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", "ImpactFactor");
			Out.put(obj, "value", f.getValue());
			Out.put(obj, "formula", f.getFormula());
			Out.put(obj, "flow", f.getFlow(), refFn);
			Out.put(obj, "unit", f.getUnit(), null);
			FlowProperty property = null;
			if (f.getFlowPropertyFactor() != null)
				property = f.getFlowPropertyFactor().getFlowProperty();
			Out.put(obj, "flowProperty", property, refFn);
			Out.put(obj, "uncertainty", Uncertainties.map(f.getUncertainty()));
			array.add(obj);
		}
		Out.put(json, "impactFactors", array);
	}
}
