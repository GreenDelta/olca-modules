package org.openlca.jsonld.input;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

abstract class ExchangeImport<P extends RootEntity> extends BaseEmbeddedImport<Exchange, P> {

	private ExchangeImport(ModelType parentType, String parentRefId, ImportConfig conf) {
		super(parentType, parentRefId, conf);
	}

	static <AP extends RootEntity> Exchange run(ModelType parentType, String parentRefId, JsonObject json,
			ImportConfig conf, Function<AP, List<Exchange>> getExchanges) {
		return new ExchangeImport<AP>(parentType, parentRefId, conf) {

			Exchange getPersisted(AP parent, JsonObject json) {
				return find(getExchanges.apply(parent), json);
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
		return e;
	}

	private void addAttributes(JsonObject json, Exchange e) {
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

	private void addCostEntries(JsonObject json, Exchange e, ImportConfig conf) {
		e.costFormula = Json.getString(json, "costFormula");
		e.costs = Json.getOptionalDouble(json, "costValue");
		String currencyId = Json.getRefId(json, "currency");
		if (currencyId != null)
			e.currency = CurrencyImport.run(currencyId, conf);
	}

	private void addExchangeRefs(JsonObject json, Exchange e, ImportConfig conf) {
		Flow flow = FlowImport.run(Json.getRefId(json, "flow"), conf);
		e.flow = flow;
		if (flow == null)
			return;
		String propId = Json.getRefId(json, "flowProperty");
		if (propId != null) {
			for (FlowPropertyFactor f : flow.flowPropertyFactors) {
				FlowProperty prop = f.flowProperty;
				if (prop == null)
					continue;
				if (Objects.equals(propId, prop.refId)) {
					e.flowPropertyFactor = f;
					break;
				}
			}
		} else {
			e.flowPropertyFactor = e.flow.getReferenceFactor();
		}
		String unitId = Json.getRefId(json, "unit");
		if (unitId != null) {
			e.unit = conf.db.get(ModelType.UNIT, unitId);
		} else if (e.flowPropertyFactor != null) {
			e.unit = e.flowPropertyFactor.flowProperty.unitGroup.referenceUnit;
		}
	}

	Exchange find(List<Exchange> exchanges, JsonObject json) {
		int internalId = Json.getInt(json, "internalId", -1);
		for (Exchange exchange : exchanges)
			if (exchange.internalId == internalId)
				return exchange;
		return null;
	}

}
