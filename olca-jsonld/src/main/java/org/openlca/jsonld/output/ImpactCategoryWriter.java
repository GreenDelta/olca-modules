package org.openlca.jsonld.output;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Uncertainty;
import org.openlca.jsonld.EntityStore;

class ImpactCategoryWriter implements Writer<ImpactCategory> {

	private EntityStore store;

	public ImpactCategoryWriter() {
	}

	public ImpactCategoryWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void write(ImpactCategory category) {
		if (category == null || store == null)
			return;
		if (store.contains(ModelType.IMPACT_CATEGORY, category.getRefId()))
			return;
		JsonObject obj = serialize(category, null, null);
		store.add(ModelType.IMPACT_CATEGORY, category.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(ImpactCategory category, Type type,
			JsonSerializationContext context) {
		JsonObject obj = store == null ? new JsonObject() : store.initJson();
		JsonExport.addAttributes(category, obj, store);
		obj.addProperty("referenceUnit", category.getReferenceUnit());
		JsonArray array = new JsonArray();
		for (ImpactFactor factor : category.getImpactFactors()) {
			JsonObject factorObj = map(factor);
			array.add(factorObj);
		}
		obj.add("impactFactors", array);
		return obj;
	}

	private JsonObject map(ImpactFactor factor) {
		JsonObject obj = new JsonObject();
		obj.addProperty("@type", "ImpactFactor");
		if (factor.getId() != 0)
			obj.addProperty("@id", factor.getId());
		obj.addProperty("value", factor.getValue());
		obj.addProperty("formula", factor.getFormula());
		obj.add("flow", Out.put(factor.getFlow(), store));
		obj.add("unit", Out.createRef(factor.getUnit()));
		FlowPropertyFactor fp = factor.getFlowPropertyFactor();
		if (fp != null) {
			JsonObject fpObj = new JsonObject();
			new FlowPropertyFactorWriter(store).map(fp, fpObj);
			obj.add("flowPropertyFactor", fpObj);
		}
		Uncertainty uncertainty = factor.getUncertainty();
		if (uncertainty != null) {
			JsonObject uncertaintyObj = new JsonObject();
			UncertaintyWriter.map(uncertainty, uncertaintyObj);
			obj.add("uncertainty", uncertaintyObj);
		}
		return obj;
	}
}
