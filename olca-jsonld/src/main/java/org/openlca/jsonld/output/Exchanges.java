package org.openlca.jsonld.output;

import java.util.UUID;
import java.util.function.Consumer;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;

import com.google.gson.JsonObject;

class Exchanges {

	static String map(Exchange e, JsonObject obj, ExportConfig conf,
			Consumer<RootEntity> refFn) {
		if (e == null || obj == null)
			return null;
		String internalId = UUID.randomUUID().toString();
		obj.addProperty("@id", internalId);
		obj.addProperty("@type", "Exchange");
		obj.addProperty("avoidedProduct", e.isAvoidedProduct());
		obj.addProperty("input", e.isInput());
		obj.addProperty("baseUncertainty", e.getBaseUncertainty());
		obj.addProperty("amount", e.getAmountValue());
		obj.addProperty("amountFormula", e.getAmountFormula());
		obj.addProperty("pedigreeUncertainty", e.getPedigreeUncertainty());
		obj.addProperty("costFormula", e.costFormula);
		obj.addProperty("costValue", e.costValue);
		if (e.currency != null)
			obj.add("currency", References.create(e.currency, refFn));
		if (e.costCategory != null)
			obj.add("costCategory", References.create(e.costCategory, refFn));
		mapRefs(e, obj, conf, refFn);
		return internalId;
	}

	private static void mapRefs(Exchange e, JsonObject obj, ExportConfig conf,
			Consumer<RootEntity> refFn) {
		obj.add("defaultProvider", References.create(ModelType.PROCESS,
				e.getDefaultProviderId(), conf, refFn));
		obj.add("flow", References.create(e.getFlow(), refFn));
		obj.add("unit", References.create(e.getUnit()));
		FlowPropertyFactor propFac = e.getFlowPropertyFactor();
		if (propFac != null) {
			JsonObject ref = References
					.create(propFac.getFlowProperty(), refFn);
			obj.add("flowProperty", ref);
		}
		Uncertainty uncertainty = e.getUncertainty();
		if (uncertainty != null) {
			JsonObject uncertaintyObj = new JsonObject();
			Uncertainties.map(uncertainty, uncertaintyObj);
			obj.add("uncertainty", uncertaintyObj);
		}
	}

}
