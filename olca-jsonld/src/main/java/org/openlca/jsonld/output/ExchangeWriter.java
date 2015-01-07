package org.openlca.jsonld.output;

import java.lang.reflect.Type;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Uncertainty;
import org.openlca.jsonld.EntityStore;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class ExchangeWriter implements JsonSerializer<Exchange> {

	private EntityStore store;

	public ExchangeWriter() {
	}

	public ExchangeWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public JsonElement serialize(Exchange exchange, Type type,
			JsonSerializationContext jsonSerializationContext) {
		JsonObject obj = store == null ? new JsonObject() : store.initJson();
		map(exchange, obj);
		return obj;
	}

	void map(Exchange e, JsonObject obj) {
		if (e == null || obj == null)
			return;
		obj.addProperty("@type", "Exchange");
		if (e.getId() != 0)
			obj.addProperty("@id", e.getId());
		obj.addProperty("avoidedProduct", e.isAvoidedProduct());
		obj.addProperty("input", e.isInput());
		obj.addProperty("baseUncertainty", e.getBaseUncertainty());
		obj.addProperty("amount", e.getAmountValue());
		obj.addProperty("amountFormula", e.getAmountFormula());
		obj.addProperty("pedigreeUncertainty", e.getPedigreeUncertainty());
		mapObjectRefs(e, obj);
	}

	private void mapObjectRefs(Exchange e, JsonObject obj) {
		// TODO: default providers -> we need the database
		obj.add("flow", Out.put(e.getFlow(), store));
		obj.add("unit", Out.createRef(e.getUnit()));
		FlowPropertyFactor propFac = e.getFlowPropertyFactor();
		if (propFac != null) {
			JsonObject facObj = new JsonObject();
			new FlowPropertyFactorWriter(store).map(propFac, facObj);
			obj.add("flowPropertyFactor", facObj);
		}
		Uncertainty uncertainty = e.getUncertainty();
		if (uncertainty != null) {
			JsonObject uncertaintyObj = new JsonObject();
			UncertaintyWriter.map(uncertainty, uncertaintyObj);
			obj.add("uncertainty", uncertaintyObj);
		}
	}
}
