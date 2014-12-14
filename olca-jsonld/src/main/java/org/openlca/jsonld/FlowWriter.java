package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class FlowWriter implements Writer<Flow> {

	private EntityStore store;
	private boolean writeContext = true;

	public FlowWriter() {
	}

	public FlowWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void skipContext() {
		this.writeContext = false;
	}

	@Override
	public void write(Flow flow) {
		if (flow == null || store == null)
			return;
		if (store.contains(ModelType.FLOW, flow.getRefId()))
			return;
		JsonObject obj = serialize(flow, null, null);
		store.add(ModelType.FLOW, flow.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(Flow flow, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		if (writeContext)
			JsonWriter.addContext(obj);
		map(flow, obj);
		return obj;
	}

	void map(Flow flow, JsonObject obj) {
		if (flow == null || obj == null)
			return;
		JsonWriter.addAttributes(flow, obj, store);
		if (flow.getFlowType() != null)
			obj.addProperty("flowType", flow.getFlowType().name());
		obj.addProperty("cas", flow.getCasNumber());
		obj.addProperty("formula", flow.getFormula());
		JsonObject locationRef = Refs.put(flow.getLocation(), store);
		obj.add("location", locationRef);
		JsonObject propRef = Refs.put(flow.getReferenceFlowProperty(), store);
		obj.add("referenceFlowProperty", propRef);
		addFactors(flow, obj);
	}

	private void addFactors(Flow flow, JsonObject obj) {
		JsonArray factorArray = new JsonArray();
		for (FlowPropertyFactor factor : flow.getFlowPropertyFactors()) {
			JsonObject factorObj = new JsonObject();
			JsonObject propRef = Refs.put(factor.getFlowProperty(), store);
			factorObj.addProperty("@type", "FlowPropertyFactor");
			factorObj.add("flowProperty", propRef);
			factorObj.addProperty("value", factor.getConversionFactor());
			factorArray.add(factorObj);
		}
		obj.add("flowPropertyFactors", factorArray);
	}

}
