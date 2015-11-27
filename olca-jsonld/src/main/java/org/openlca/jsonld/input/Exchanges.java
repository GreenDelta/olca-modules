package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Exchanges {

	static ExchangeWithId map(JsonObject json, ImportConfig conf) {
		Exchange e = new Exchange();
		e.setAvoidedProduct(In.getBool(json, "avoidedProduct", false));
		e.setInput(In.getBool(json, "input", false));
		e.setBaseUncertainty(In.getOptionalDouble(json, "baseUncertainty"));
		e.setAmountValue(In.getDouble(json, "amount", 0));
		e.setAmountFormula(In.getString(json, "amountFormula"));
		e.setPedigreeUncertainty(In.getString(json, "pedigreeUncertainty"));
		String providerId = In.getRefId(json, "defaultProvider");
		if (providerId != null)
			e.setDefaultProviderId(ProcessImport.run(providerId, conf).getId());
		addCostEntries(json, e, conf);
		JsonElement u = json.get("uncertainty");
		if (u != null && u.isJsonObject())
			e.setUncertainty(Uncertainties.read(u.getAsJsonObject()));
		addExchangeRefs(json, e, conf);
		String internalId = In.getString(json, "@id");
		return new ExchangeWithId(internalId, e);
	}

	private static void addCostEntries(JsonObject json, Exchange e,
			ImportConfig conf) {
		e.costFormula = In.getString(json, "costFormula");
		e.costValue = In.getOptionalDouble(json, "costValue");
		String currencyId = In.getRefId(json, "currency");
		if (currencyId != null)
			e.currency = CurrencyImport.run(currencyId, conf);
		String categoryId = In.getRefId(json, "costCategory");
		if (categoryId != null)
			e.costCategory = CostCategoryImport.run(categoryId, conf);
	}

	private static void addExchangeRefs(JsonObject json, Exchange e,
			ImportConfig conf) {
		Flow flow = FlowImport.run(In.getRefId(json, "flow"), conf);
		e.setFlow(flow);
		String unitId = In.getRefId(json, "unit");
		e.setUnit(conf.db.getUnit(unitId));
		if (flow == null)
			return;
		String propId = In.getRefId(json, "flowProperty");
		for (FlowPropertyFactor f : flow.getFlowPropertyFactors()) {
			FlowProperty prop = f.getFlowProperty();
			if (prop == null)
				continue;
			if (Objects.equals(propId, prop.getRefId())) {
				e.setFlowPropertyFactor(f);
				break;
			}
		}
	}

	static class ExchangeWithId {

		final String internalId;
		final Exchange exchange;

		private ExchangeWithId(String internalId, Exchange exchange) {
			this.internalId = internalId;
			this.exchange = exchange;
		}

	}

}
