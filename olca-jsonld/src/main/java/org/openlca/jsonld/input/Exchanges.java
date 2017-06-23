package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Exchanges {

	static ExchangeWithId map(JsonObject json, ImportConfig conf) {
		Exchange e = new Exchange();
		addAttributes(json, e);
		addProvider(json, e, conf);
		addCostEntries(json, e, conf);
		addExchangeRefs(json, e, conf);
		String internalId = In.getString(json, "@id");
		return new ExchangeWithId(internalId, e);
	}

	private static void addAttributes(JsonObject json, Exchange e) {
		e.isAvoided = In.getBool(json, "avoidedProduct", false);
		e.isInput = In.getBool(json, "input", false);
		e.baseUncertainty = In.getOptionalDouble(json, "baseUncertainty");
		e.amount = In.getDouble(json, "amount", 0);
		e.amountFormula = In.getString(json, "amountFormula");
		e.dqEntry = In.getString(json, "dqEntry");
		e.description = In.getString(json, "description");
		JsonElement u = json.get("uncertainty");
		if (u != null && u.isJsonObject()) {
			e.uncertainty = Uncertainties.read(u.getAsJsonObject());
		}
	}

	private static void addProvider(JsonObject json, Exchange e, ImportConfig conf) {
		String providerId = In.getRefId(json, "defaultProvider");
		if (providerId == null)
			return;
		Process provider = ProcessImport.run(providerId, conf);
		if (provider == null)
			return;
		e.defaultProviderId = provider.getId();
	}

	private static void addCostEntries(JsonObject json, Exchange e,
			ImportConfig conf) {
		e.costFormula = In.getString(json, "costFormula");
		e.costs = In.getOptionalDouble(json, "costValue");
		String currencyId = In.getRefId(json, "currency");
		if (currencyId != null)
			e.currency = CurrencyImport.run(currencyId, conf);
	}

	private static void addExchangeRefs(JsonObject json, Exchange e,
			ImportConfig conf) {
		Flow flow = FlowImport.run(In.getRefId(json, "flow"), conf);
		final Flow flow1 = flow;
		e.flow = flow1;
		String unitId = In.getRefId(json, "unit");
		e.unit = conf.db.getUnit(unitId);
		if (flow == null)
			return;
		String propId = In.getRefId(json, "flowProperty");
		for (FlowPropertyFactor f : flow.getFlowPropertyFactors()) {
			FlowProperty prop = f.getFlowProperty();
			if (prop == null)
				continue;
			if (Objects.equals(propId, prop.getRefId())) {
				e.flowPropertyFactor = f;
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
