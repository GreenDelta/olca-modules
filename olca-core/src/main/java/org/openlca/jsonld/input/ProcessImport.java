package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.SocialAspect;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProcessImport extends BaseImport<Process> {

	private final Map<Integer, Exchange> exchangeMap = new HashMap<>();

	private ProcessImport(String refId, JsonImport conf) {
		super(ModelType.PROCESS, refId, conf);
	}

	static Process run(String refId, JsonImport conf) {
		return new ProcessImport(refId, conf).run();
	}

	@Override
	Process map(JsonObject json, long id) {
		if (json == null)
			return null;
		Process p = new Process();
		In.mapAtts(json, p, id, conf);

		p.processType = getType(json);
		p.infrastructureProcess = Json.getBool(json, "infrastructureProcess", false);
		p.defaultAllocationMethod = Json.getEnum(json, "defaultAllocationMethod", AllocationMethod.class);
		p.documentation = ProcessDocReader.read(json, conf);
		String locId = Json.getRefId(json, "location");
		if (locId != null)
			p.location = LocationImport.run(locId, conf);

		// DQ systems
		String dqSystemId = Json.getRefId(json, "dqSystem");
		if (dqSystemId != null)
			p.dqSystem = DQSystemImport.run(dqSystemId, conf);
		p.dqEntry = Json.getString(json, "dqEntry");
		String exchangeDqSystemId = Json.getRefId(json, "exchangeDqSystem");
		if (exchangeDqSystemId != null)
			p.exchangeDqSystem = DQSystemImport.run(exchangeDqSystemId, conf);
		String socialDqSystemId = Json.getRefId(json, "socialDqSystem");
		if (socialDqSystemId != null)
			p.socialDqSystem = DQSystemImport.run(socialDqSystemId, conf);

		addParameters(json, p);
		addExchanges(json, p);
		addSocialAspects(json, p);
		addAllocationFactors(json, p);
		p = conf.db.put(p);
		conf.providers().pop(p);
		return p;
	}

	private ProcessType getType(JsonObject json) {
		ProcessType type = Json.getEnum(json, "processType", ProcessType.class);
		if (type == null) // support old versions with typo
			type = Json.getEnum(json, "processTyp", ProcessType.class);
		return type;
	}

	private void addParameters(JsonObject json, Process p) {
		JsonArray parameters = Json.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (JsonElement e : parameters) {
			if (!e.isJsonObject())
				continue;
			JsonObject o = e.getAsJsonObject();
			Parameter parameter = new Parameter();
			ParameterImport.mapFields(o, parameter);
			p.parameters.add(parameter);
		}
	}

	private void addExchanges(JsonObject json, Process p) {
		JsonArray exchanges = Json.getArray(json, "exchanges");
		if (exchanges == null || exchanges.size() == 0)
			return;
		p.lastInternalId = Json.getInt(json, "lastInternalId", 0);
		for (JsonElement e : exchanges) {
			if (!e.isJsonObject())
				continue;
			JsonObject o = e.getAsJsonObject();
			Exchange ex = ExchangeImport.run(ModelType.PROCESS, p.refId, o, conf,
					(Process process) -> process.exchanges);
			if (ex.internalId == 0) {
				ex.internalId = ++p.lastInternalId;
			}
			exchangeMap.put(ex.internalId, ex);
			String providerRefId = Json.getRefId(o, "defaultProvider");
			if (providerRefId != null) {
				conf.providers().add(providerRefId, ex);
			}
			p.exchanges.add(ex);
			boolean isRef = Json.getBool(o, "quantitativeReference", false);
			if (isRef)
				p.quantitativeReference = ex;
		}
	}

	private void addSocialAspects(JsonObject json, Process p) {
		JsonArray aspects = Json.getArray(json, "socialAspects");
		if (aspects == null || aspects.size() == 0)
			return;
		for (JsonElement a : aspects) {
			if (!a.isJsonObject())
				continue;
			JsonObject o = a.getAsJsonObject();
			SocialAspect aspect = aspect(o);
			p.socialAspects.add(aspect);
		}
	}

	private SocialAspect aspect(JsonObject json) {
		SocialAspect a = new SocialAspect();
		a.indicator = SocialIndicatorImport.run(Json.getRefId(json, "socialIndicator"), conf);
		a.comment = Json.getString(json, "comment");
		a.quality = Json.getString(json, "quality");
		a.rawAmount = Json.getString(json, "rawAmount");
		a.activityValue = Json.getDouble(json, "activityValue", 0d);
		a.riskLevel = Json.getEnum(json, "riskLevel", RiskLevel.class);
		a.source = SourceImport.run(Json.getRefId(json, "source"), conf);
		return a;
	}

	private void addAllocationFactors(JsonObject json, Process p) {
		JsonArray factors = Json.getArray(json, "allocationFactors");
		if (factors == null || factors.size() == 0)
			return;
		for (JsonElement f : factors) {
			if (!f.isJsonObject())
				continue;
			JsonObject o = f.getAsJsonObject();
			AllocationFactor factor = allocationFactor(o);
			if (factor != null) {
				p.allocationFactors.add(factor);
			}
		}
	}

	private AllocationFactor allocationFactor(JsonObject json) {
		String productId = Json.getRefId(json, "product");
		Flow product = FlowImport.run(productId, conf);
		if (product == null)
			return null;
		AllocationFactor factor = new AllocationFactor();
		factor.productId = product.id;
		Integer exchangeId = null;
		JsonObject exchange = Json.getObject(json, "exchange");
		if (exchange != null)
			exchangeId = Json.getInt(exchange, "internalId", 0);
		if (exchangeId != null && exchangeId != 0)
			factor.exchange = exchangeMap.get(exchangeId);
		factor.value = Json.getDouble(json, "value", 1);
		var formula = Json.getString(json, "formula");
		if (!Strings.nullOrEmpty(formula)) {
			factor.formula = formula;
		}
		factor.method = Json.getEnum(
				json, "allocationType", AllocationMethod.class);
		return factor;
	}

}
