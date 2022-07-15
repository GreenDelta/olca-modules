package org.openlca.jsonld.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.util.Strings;

public class ProcessReader implements EntityReader<Process> {

	private final EntityResolver resolver;
	private final TIntObjectHashMap<Exchange> exchanges = new TIntObjectHashMap<>();

	public ProcessReader(EntityResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Process read(JsonObject json) {
		var process = new Process();
		update(process, json);
		return process;
	}

	@Override
	public void update(Process p, JsonObject json) {
		Util.mapBase(p, json, resolver);
		p.processType = Json.getEnum(json, "processType", ProcessType.class);
		p.infrastructureProcess = Json.getBool(json, "isInfrastructureProcess", false);
		p.defaultAllocationMethod = Json.getEnum(json, "defaultAllocationMethod", AllocationMethod.class);
		p.documentation = ProcessDocs.read(json, resolver);

		var locId = Json.getRefId(json, "location");
		p.location = resolver.get(Location.class, locId);

		// DQ systems
		var dqSystemId = Json.getRefId(json, "dqSystem");
		p.dqSystem = resolver.get(DQSystem.class, dqSystemId);
		p.dqEntry = Json.getString(json, "dqEntry");
		var exchangeDqSystemId = Json.getRefId(json, "exchangeDqSystem");
		p.exchangeDqSystem = resolver.get(DQSystem.class, exchangeDqSystemId);
		var socialDqSystemId = Json.getRefId(json, "socialDqSystem");
		p.socialDqSystem = resolver.get(DQSystem.class, socialDqSystemId);

		mapParameters(json, p);
		mapExchanges(json, p);
		mapSocialAspects(json, p);
		mapAllocationFactors(json, p);
	}

	private void mapParameters(JsonObject json, Process p) {
		p.parameters.clear();
		var parameters = Json.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (var e : parameters) {
			if (!e.isJsonObject())
				continue;
			var o = e.getAsJsonObject();
			var param = new Parameter();
			ParameterReader.mapFields(param, o, resolver);
			param.scope = ParameterScope.PROCESS;
			p.parameters.add(param);
		}
	}

	private void mapExchanges(JsonObject json, Process p) {

		// index the old exchanges, that we may update
		var oldIdx = new TIntObjectHashMap<Exchange>();
		for (var old : p.exchanges) {
			oldIdx.put(old.internalId, old);
		}
		p.quantitativeReference = null;
		p.exchanges.clear();

		var array = Json.getArray(json, "exchanges");
		if (array == null || array.size() == 0)
			return;

		p.lastInternalId = Json.getInt(json, "lastInternalId", 0);
		for (var elem : array) {
			if (!elem.isJsonObject())
				continue;
			var o = elem.getAsJsonObject();

			// read / allocate exchange with ID
			int internalId = Json.getInt(o, "internalId", -1);
			var e = oldIdx.get(internalId);
			if (e == null) {
				e = new Exchange();
				e.internalId = internalId == -1
					?  ++p.lastInternalId
				: internalId;
			}
			exchanges.put(e.internalId, e);

			// provider
			var providerId = Json.getRefId(o, "defaultProvider");
			if (providerId != null) {
				resolver.resolveProvider(providerId, e);
			} else {
				e.defaultProviderId = 0;
			}

			// flow and quantity
			e.flow = resolver.get(Flow.class, Json.getRefId(o, "flow"));
			var quantity = Quantity.of(e.flow, o);
			e.flowPropertyFactor = quantity.factor();
			e.unit = quantity.unit();

			// general attributes
			e.isInput = Json.getBool(o, "isInput", false);
			e.amount = Json.getDouble(o, "amount", 0);
			e.formula = Json.getString(o, "amountFormula");
			e.isAvoided = Json.getBool(o, "isAvoidedProduct", false);
			e.description = Json.getString(o, "description");
			e.dqEntry = Json.getString(o, "dqEntry");
			var baseUnc = Json.getDouble(o, "baseUncertainty");
			e.baseUncertainty = baseUnc.isPresent()
				? baseUnc.getAsDouble()
				: null;
			var u = Json.getObject(o, "uncertainty");
			e.uncertainty = u != null
				? Uncertainties.read(u)
				: null;

			// costs
			e.costFormula = Json.getString(o, "costFormula");
			var costs = Json.getDouble(o, "costValue");
			e.costs = costs.isPresent() ? costs.getAsDouble() : null;
			var currencyId = Json.getRefId(o, "currency");
			e.currency = resolver.get(Currency.class, currencyId);

			// location
			var locationId = Json.getRefId(o, "location");
			e.location = resolver.get(Location.class, locationId);

			p.exchanges.add(e);
			boolean isRef = Json.getBool(o, "isQuantitativeReference", false);
			if (isRef) {
				p.quantitativeReference = e;
			}
		}
	}

	private void mapSocialAspects(JsonObject json, Process p) {
		p.socialAspects.clear();
		Json.forEachObject(json, "socialAspects", obj -> {
			var a = new SocialAspect();
			var indicatorId = Json.getRefId(obj, "socialIndicator");
			a.indicator = resolver.get(SocialIndicator.class, indicatorId);
			a.comment = Json.getString(obj, "comment");
			a.quality = Json.getString(obj, "quality");
			a.rawAmount = Json.getString(obj, "rawAmount");
			a.activityValue = Json.getDouble(obj, "activityValue", 0);
			a.riskLevel = Json.getEnum(obj, "riskLevel", RiskLevel.class);
			var sourceId = Json.getRefId(obj, "source");
			a.source = resolver.get(Source.class, sourceId);
			p.socialAspects.add(a);
		});
	}

	private void mapAllocationFactors(JsonObject json, Process p) {
		p.allocationFactors.clear();
		Json.forEachObject(json, "allocationFactors", obj -> {
			var productId = Json.getRefId(obj, "product");
			var product = resolver.get(Flow.class, productId);
			if (product == null)
				return;
			var factor = new AllocationFactor();
			factor.productId = product.id;
			var exchange = Json.getObject(obj, "exchange");
			if (exchange != null) {
				int exchangeId = Json.getInt(exchange, "internalId", -1);
				factor.exchange = exchanges.get(exchangeId);
			}
			factor.value = Json.getDouble(obj, "value", 1);
			var formula = Json.getString(obj, "formula");
			if (Strings.notEmpty(formula)) {
				factor.formula = formula;
			}
			factor.method = Json.getEnum(
				obj, "allocationType", AllocationMethod.class);
			p.allocationFactors.add(factor);
		});
	}
}
