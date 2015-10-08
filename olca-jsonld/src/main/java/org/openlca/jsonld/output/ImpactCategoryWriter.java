package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ImpactCategoryWriter extends Writer<ImpactCategory> {

	@Override
	JsonObject write(ImpactCategory category, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(category, refFn);
		if (obj == null)
			return null;
		obj.addProperty("referenceUnitName", category.getReferenceUnit());
		JsonArray array = new JsonArray();
		for (ImpactFactor factor : category.getImpactFactors()) {
			JsonObject factorObj = map(factor, refFn);
			array.add(factorObj);
		}
		obj.add("impactFactors", array);
		return obj;
	}

	private JsonObject map(ImpactFactor factor, Consumer<RootEntity> refFn) {
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ImpactFactor");
		obj.addProperty("value", factor.getValue());
		obj.addProperty("formula", factor.getFormula());
		obj.add("flow", createRef(factor.getFlow(), refFn));
		obj.add("unit", createRef(factor.getUnit()));
		FlowPropertyFactor fp = factor.getFlowPropertyFactor();
		if (fp != null) {
			JsonObject ref = createRef(fp.getFlowProperty(), refFn);
			obj.add("flowProperty", ref);
		}
		Uncertainty uncertainty = factor.getUncertainty();
		if (uncertainty != null) {
			JsonObject uncertaintyObj = new JsonObject();
			Uncertainties.map(uncertainty, uncertaintyObj);
			obj.add("uncertainty", uncertaintyObj);
		}
		return obj;
	}
}
