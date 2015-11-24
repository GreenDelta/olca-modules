package org.openlca.jsonld.output;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.ExchangeKey;

import com.google.gson.JsonObject;

class Exchanges {

	static String map(Exchange e, String processRefId, JsonObject obj,
			ExportConfig conf) {
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
		Out.put(obj, "currency", e.currency, conf);
		Out.put(obj, "costCategory", e.costCategory, conf);
		String providerRefId = mapRefs(e, obj, conf);
		String internalId = ExchangeKey.get(processRefId, providerRefId, e);
		Out.put(obj, "@id", internalId);
		return internalId;
	}

	private static String mapRefs(Exchange e, JsonObject obj, ExportConfig conf) {
		Long pId = e.getDefaultProviderId();
		JsonObject p = null;
		if (conf.exportProviders)
			p = References.create(ModelType.PROCESS, pId, conf, false);
		else
			p = References.create(new ProcessDao(conf.db).getDescriptor(pId));
		Out.put(obj, "defaultProvider", p);
		Out.put(obj, "flow", e.getFlow(), conf);
		Out.put(obj, "unit", e.getUnit(), conf);
		FlowProperty property = null;
		if (e.getFlowPropertyFactor() != null)
			property = e.getFlowPropertyFactor().getFlowProperty();
		Out.put(obj, "flowProperty", property, conf);
		Out.put(obj, "uncertainty", Uncertainties.map(e.getUncertainty()));
		if (p == null)
			return null;
		return p.get("@id").getAsString();
	}

}
