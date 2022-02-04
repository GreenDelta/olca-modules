package org.openlca.jsonld.input;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.google.gson.JsonObject;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
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
		addAttributes(json, e);
		addCostEntries(json, e, conf);
		addExchangeRefs(json, e, conf);

		// exchange location
		var locationId = Json.getRefId(json, "location");
		if (Strings.notEmpty(locationId)) {
			e.location = LocationImport.run(locationId, conf);
		}

		return e;
	}

	private void addAttributes(JsonObject json, Exchange e) {
		e.isAvoided = Json.getBool(json, "avoidedProduct", false);
		e.isInput = Json.getBool(json, "input", false);
		e.baseUncertainty = Json.getDouble(json, "baseUncertainty").orElse(null);
		e.amount = Json.getDouble(json, "amount", 0);
		e.formula = Json.getString(json, "amountFormula");
		e.dqEntry = Json.getString(json, "dqEntry");
		e.description = Json.getString(json, "description");
		e.internalId = Json.getInt(json, "internalId", 0);
		var u = Json.getObject(json, "uncertainty");
		if (u != null) {
			e.uncertainty = Uncertainties.read(u);
		}
	}

	private void addCostEntries(JsonObject json, Exchange e, JsonImport conf) {
		e.costFormula = Json.getString(json, "costFormula");
		e.costs = Json.getDouble(json, "costValue").orElse(null);
		String currencyId = Json.getRefId(json, "currency");
		if (currencyId != null)
			e.currency = CurrencyImport.run(currencyId, conf);
	}

	private void addExchangeRefs(JsonObject json, Exchange e, JsonImport conf) {
		var flow = FlowImport.run(Json.getRefId(json, "flow"), conf);
		e.flow = flow;
		if (flow == null)
			return;

		e.flowPropertyFactor = e.flow.getReferenceFactor();
		String propId = Json.getRefId(json, "flowProperty");
		if (Strings.nullOrEmpty(propId)) {
			e.flowPropertyFactor = e.flow.getReferenceFactor();
		} else {
			for (var f : flow.flowPropertyFactors) {
				var prop = f.flowProperty;
				if (prop == null)
					continue;
				if (Objects.equals(propId, prop.refId)) {
					e.flowPropertyFactor = f;
					break;
				}
			}
		}

		e.unit = unitOf(e, json);
	}

	private Unit unitOf(Exchange e, JsonObject exchangeObj) {

		// first try to get it by reference ID
		var unitObj = Json.getObject(exchangeObj, "unit");
		var unitID = unitObj != null
			? Json.getString(unitObj, "@id")
			: null;
		if (Strings.notEmpty(unitID))
			return conf.db.get(ModelType.UNIT, unitID);

		if (e.flowPropertyFactor == null
			|| e.flowPropertyFactor.flowProperty == null)
			return null;
		var units = e.flowPropertyFactor.flowProperty.unitGroup;
		if (units == null)
			return null;

		var name = Json.getString(unitObj, "name");
		return Strings.notEmpty(name)
			? units.getUnit(name)
			: units.referenceUnit;
	}

}
