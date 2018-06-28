package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.SocialAspect;
import org.openlca.jsonld.Json;
import org.openlca.util.DQSystems;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProcessImport extends BaseImport<Process> {

	private Map<Integer, Exchange> exchangeMap = new HashMap<>();

	private ProcessImport(String refId, ImportConfig conf) {
		super(ModelType.PROCESS, refId, conf);
	}

	static Process run(String refId, ImportConfig conf) {
		return new ProcessImport(refId, conf).run();
	}

	@Override
	Process map(JsonObject json, long id) {
		if (json == null)
			return null;
		Process p = new Process();
		In.mapAtts(json, p, id, conf);
		p.setProcessType(getType(json));
		p.setInfrastructureProcess(Json.getBool(json, "infrastructureProcess", false));
		p.setDefaultAllocationMethod(Json.getEnum(json, "defaultAllocationMethod", AllocationMethod.class));
		ProcessDocumentation doc = ProcessDocReader.read(json, conf);
		p.setDocumentation(doc);
		String locId = Json.getRefId(json, "location");
		if (locId != null)
			p.setLocation(LocationImport.run(locId, conf));
		String dqSystemId = Json.getRefId(json, "dqSystem");
		if (dqSystemId != null)
			p.dqSystem = DQSystemImport.run(dqSystemId, conf);
		p.dqEntry = Json.getString(json, "dqEntry");
		String exchangeDqSystemId = Json.getRefId(json, "exchangeDqSystem");
		if (exchangeDqSystemId != null)
			p.exchangeDqSystem = DQSystemImport.run(exchangeDqSystemId, conf);
		checkPedigreeSystem(p);
		String socialDqSystemId = Json.getRefId(json, "socialDqSystem");
		if (socialDqSystemId != null)
			p.socialDqSystem = DQSystemImport.run(socialDqSystemId, conf);
		String curId = Json.getRefId(json, "currency");
		if (curId != null)
			p.currency = CurrencyImport.run(curId, conf);
		addParameters(json, p);
		// avoid cyclic reference problems
		if (hasDefaultProviders(json))
			p = conf.db.put(p);
		addExchanges(json, p);
		addSocialAspects(json, p);
		addAllocationFactors(json, p);
		return conf.db.put(p);
	}

	private void checkPedigreeSystem(Process p) {
		if (p.exchangeDqSystem != null)
			// set another system, so everything all right
			return;
		for (Exchange e : p.getExchanges()) {
			if (e.dqEntry == null)
				continue;
			p.exchangeDqSystem = new DQSystemDao(conf.db.getDatabase()).insert(DQSystems.ecoinvent());
			return;
		}
	}

	private boolean hasDefaultProviders(JsonObject json) {
		JsonArray exchanges = Json.getArray(json, "exchanges");
		if (exchanges == null || exchanges.size() == 0)
			return false;
		for (JsonElement e : exchanges) {
			if (!e.isJsonObject())
				continue;
			String providerRefId = Json.getRefId(e.getAsJsonObject(), "defaultProvider");
			if (providerRefId != null)
				return true;
		}
		return false;
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
			String refId = Json.getString(o, "@id");
			ParameterImport pi = new ParameterImport(refId, conf);
			Parameter parameter = new Parameter();
			pi.mapFields(o, parameter);
			p.getParameters().add(parameter);
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
			Exchange ex = Exchanges.map(o, conf);
			if (ex.internalId == 0) {
				ex.internalId = ++p.lastInternalId;
			}
			exchangeMap.put(ex.internalId, ex);
			String providerRefId = Json.getRefId(o, "defaultProvider");
			if (providerRefId != null) {
				conf.putProviderInfo(p.getRefId(), ex.internalId, providerRefId);
			}
			p.getExchanges().add(ex);
			boolean isRef = Json.getBool(o, "quantitativeReference", false);
			if (isRef)
				p.setQuantitativeReference(ex);
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
				p.getAllocationFactors().add(factor);
			}
		}
	}

	private AllocationFactor allocationFactor(JsonObject json) {
		String productId = Json.getRefId(json, "product");
		Flow product = FlowImport.run(productId, conf);
		if (product == null)
			return null;
		AllocationFactor factor = new AllocationFactor();
		factor.setProductId(product.getId());
		Integer exchangeId = null;
		JsonObject exchange = Json.getObject(json, "exchange");
		if (exchange != null)
			exchangeId = Json.getInt(exchange, "internalId", 0);
		if (exchangeId != null && exchangeId != 0)
			factor.setExchange(exchangeMap.get(exchangeId));
		factor.setValue(Json.getDouble(json, "value", 1));
		factor.setAllocationType(Json.getEnum(json, "allocationType", AllocationMethod.class));
		return factor;
	}

}