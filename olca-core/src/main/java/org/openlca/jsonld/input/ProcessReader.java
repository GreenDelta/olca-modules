package org.openlca.jsonld.input;

import org.openlca.core.io.EntityResolver;
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
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

import gnu.trove.map.hash.TIntObjectHashMap;

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
		// TODO p.documentation = ProcessDocReader.read(json, conf);

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

		readParameters(json, p);
		readExchanges(json, p);
		addSocialAspects(json, p);
		addAllocationFactors(json, p);
	}

	private void readParameters(JsonObject json, Process p) {
		p.parameters.clear();
		var parameters = Json.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (var e : parameters) {
			if (!e.isJsonObject())
				continue;
			var o = e.getAsJsonObject();
			var param = new Parameter();
			ParameterReader.mapFields(param, o);
			param.scope = ParameterScope.PROCESS;
			p.parameters.add(param);
		}
	}

	private void readExchanges(JsonObject _json, Process p) {

		var array = Json.getArray(json, "exchanges");
		if (array == null || array.size() == 0) {
			p.exchanges.clear();
			p.quantitativeReference = null;
			return;
		}

		// index the old exchanges, that we may update
		var oldIdx = new TIntObjectHashMap<Exchange>();
		for (var old : p.exchanges) {
			oldIdx.put(old.internalId, old);
		}

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
			}
			if (e.internalId == 0) {
				e.internalId = ++p.lastInternalId;
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
			if (e.flow != null) {
				var quantity = Quantity.of(e.flow, o);
				e.flowPropertyFactor = quantity.factor();
				e.unit = quantity.unit();
			}

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

			p.exchanges.add(e);
			boolean isRef = Json.getBool(o, "isQuantitativeReference", false);
			if (isRef)
				p.quantitativeReference = e;
		}
	}
}
