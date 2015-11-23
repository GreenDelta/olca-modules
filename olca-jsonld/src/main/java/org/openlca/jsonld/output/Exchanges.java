package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.ExchangeKey;
import org.openlca.jsonld.output.ExportConfig.DefaultProviderOption;

import com.google.gson.JsonObject;

class Exchanges {

	static String map(Exchange e, String processRefId, JsonObject obj,
			ExportConfig conf, Consumer<RootEntity> refFn) {
		if (e == null || obj == null)
			return null;
		Out.put(obj, "@type", "Exchange");
		Out.put(obj, "avoidedProduct", e.isAvoidedProduct());
		Out.put(obj, "input", e.isInput());
		Out.put(obj, "baseUncertainty", e.getBaseUncertainty());
		Out.put(obj, "amount", e.getAmountValue());
		Out.put(obj, "amountFormula", e.getAmountFormula());
		Out.put(obj, "pedigreeUncertainty", e.getPedigreeUncertainty());
		Out.put(obj, "costFormula", e.costFormula);
		Out.put(obj, "costValue", e.costValue);
		Out.put(obj, "currency", e.currency, refFn);
		Out.put(obj, "costCategory", e.costCategory, refFn);
		String providerRefId = mapRefs(e, obj, conf, refFn);
		String internalId = ExchangeKey.get(processRefId, providerRefId, e);
		Out.put(obj, "@id", internalId);
		return internalId;
	}

	private static String mapRefs(Exchange e, JsonObject obj,
			ExportConfig conf, Consumer<RootEntity> refFn) {
		boolean exportProcess = conf.defaultProviderOption == DefaultProviderOption.INCLUDE_PROVIDER;
		Long pId = e.getDefaultProviderId();
		JsonObject provider = null;
		if (exportProcess)
			provider = References.create(ModelType.PROCESS, pId, conf, refFn);
		else
			provider = References.create(ModelType.PROCESS, pId, conf, null);
		Out.put(obj, "defaultProvider", provider);
		Out.put(obj, "flow", e.getFlow(), refFn);
		Out.put(obj, "unit", e.getUnit(), null);
		FlowProperty property = null;
		if (e.getFlowPropertyFactor() != null)
			property = e.getFlowPropertyFactor().getFlowProperty();
		Out.put(obj, "flowProperty", property, refFn);
		Out.put(obj, "uncertainty", Uncertainties.map(e.getUncertainty()));
		if (provider == null)
			return null;
		return provider.get("@id").getAsString();
	}

}
