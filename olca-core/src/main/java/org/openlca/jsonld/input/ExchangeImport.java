package org.openlca.jsonld.input;

import java.util.List;
import java.util.function.Function;

import com.google.gson.JsonObject;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

abstract class ExchangeImport<P extends RootEntity> extends BaseEmbeddedImport<Exchange, P> {

	private ExchangeImport(ModelType parentType, String parentRefId, JsonImport conf) {
		super(parentType, parentRefId, conf);
	}

	static <AP extends RootEntity> Exchange run(
		ModelType parentType, String parentRefId, JsonObject json,
		JsonImport conf, Function<AP, List<Exchange>> getExchanges) {
		return new ExchangeImport<AP>(parentType, parentRefId, conf) {

			Exchange getPersisted(AP parent, JsonObject json) {
				List<Exchange> exchanges = getExchanges.apply(parent);
				if (exchanges == null)
					return null;
				int internalId = Json.getInt(json, "internalId", -1);
				for (Exchange exchange : exchanges) {
					if (exchange.internalId == internalId)
						return exchange;
				}
				return null;
			}

		}.run(json);
	}

	@Override
	Exchange map(JsonObject json, long id) {
		Exchange e = new Exchange();
		e.id = id;

		// flow and quantity
		var flow = FlowImport.run(Json.getRefId(json, "flow"), conf);
		e.flow = flow;
		if (flow != null) {
			var quantity = Quantity.of(flow, json);
			e.flowPropertyFactor = quantity.factor();
			e.unit = quantity.unit();
		}

		// general attributes
		e.internalId = Json.getInt(json, "internalId", 0);
		e.isInput = Json.getBool(json, "input", false);
		e.amount = Json.getDouble(json, "amount", 0);
		e.formula = Json.getString(json, "amountFormula");
		e.isAvoided = Json.getBool(json, "avoidedProduct", false);
		e.baseUncertainty = Json.getDouble(json, "baseUncertainty").orElse(null);
		e.dqEntry = Json.getString(json, "dqEntry");
		e.description = Json.getString(json, "description");
		var u = Json.getObject(json, "uncertainty");
		if (u != null) {
			e.uncertainty = Uncertainties.read(u);
		}

		// costs
		e.costFormula = Json.getString(json, "costFormula");
		e.costs = Json.getDouble(json, "costValue").orElse(null);
		var currencyId = Json.getRefId(json, "currency");
		if (currencyId != null) {
			e.currency = CurrencyImport.run(currencyId, conf);
		}

		// exchange location
		var locationId = Json.getRefId(json, "location");
		if (Strings.notEmpty(locationId)) {
			e.location = LocationImport.run(locationId, conf);
		}

		return e;
	}
}
