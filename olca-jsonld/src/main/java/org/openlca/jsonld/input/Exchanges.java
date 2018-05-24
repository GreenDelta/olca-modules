package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Exchanges {

	static Exchange map(JsonObject json, ImportConfig conf) {
		Exchange e = new Exchange();
		addAttributes(json, e);
		addProvider(json, e, conf);
		addCostEntries(json, e, conf);
		addExchangeRefs(json, e, conf);
		return e;
	}

	private static void addAttributes(JsonObject json, Exchange e) {
		e.isAvoided = Json.getBool(json, "avoidedProduct", false);
		e.isInput = Json.getBool(json, "input", false);
		e.baseUncertainty = Json.getOptionalDouble(json, "baseUncertainty");
		e.amount = Json.getDouble(json, "amount", 0);
		e.amountFormula = Json.getString(json, "amountFormula");
		e.dqEntry = Json.getString(json, "dqEntry");
		e.description = Json.getString(json, "description");
		e.internalId = Json.getInt(json, "internalId", 0);
		JsonElement u = json.get("uncertainty");
		if (u != null && u.isJsonObject()) {
			e.uncertainty = Uncertainties.read(u.getAsJsonObject());
		}
	}

	private static void addProvider(JsonObject json, Exchange e, ImportConfig conf) {
		String providerId = Json.getRefId(json, "defaultProvider");
		if (providerId == null)
			return;
		Process provider = ProcessImport.run(providerId, conf);
		if (provider == null)
			return;
		e.defaultProviderId = provider.getId();
	}

	private static void addCostEntries(JsonObject json, Exchange e,
			ImportConfig conf) {
		e.costFormula = Json.getString(json, "costFormula");
		e.costs = Json.getOptionalDouble(json, "costValue");
		String currencyId = Json.getRefId(json, "currency");
		if (currencyId != null)
			e.currency = CurrencyImport.run(currencyId, conf);
	}

	private static void addExchangeRefs(JsonObject json, Exchange e,
			ImportConfig conf) {
		Flow flow = FlowImport.run(Json.getRefId(json, "flow"), conf);
		e.flow = flow;
		String unitId = Json.getRefId(json, "unit");
		e.unit = conf.db.getUnit(unitId);
		if (flow == null)
			return;
		String propId = Json.getRefId(json, "flowProperty");
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

}
