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
		p.setInfrastructureProcess(In.getBool(json, "infrastructureProcess", false));
		p.setDefaultAllocationMethod(In.getEnum(json, "defaultAllocationMethod", AllocationMethod.class));
		ProcessDocumentation doc = ProcessDocReader.read(json, conf);
		p.setDocumentation(doc);
		String locId = In.getRefId(json, "location");
		if (locId != null)
			p.setLocation(LocationImport.run(locId, conf));
		String dqSystemId = In.getRefId(json, "dqSystem");
		if (dqSystemId != null)
			p.dqSystem = DQSystemImport.run(dqSystemId, conf);
		p.dqEntry = In.getString(json, "dqEntry");
		String exchangeDqSystemId = In.getRefId(json, "exchangeDqSystem");
		if (exchangeDqSystemId != null)
			p.exchangeDqSystem = DQSystemImport.run(exchangeDqSystemId, conf);
		checkPedigreeSystem(p);
		String socialDqSystemId = In.getRefId(json, "socialDqSystem");
		if (socialDqSystemId != null)
			p.socialDqSystem = DQSystemImport.run(socialDqSystemId, conf);
		String curId = In.getRefId(json, "currency");
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
		JsonArray exchanges = In.getArray(json, "exchanges");
		if (exchanges == null || exchanges.size() == 0)
			return false;
		for (JsonElement e : exchanges) {
			if (!e.isJsonObject())
				continue;
			String providerRefId = In.getRefId(e.getAsJsonObject(), "defaultProvider");
			if (providerRefId != null)
				return true;
		}
		return false;
	}

	private ProcessType getType(JsonObject json) {
		ProcessType type = In.getEnum(json, "processType", ProcessType.class);
		if (type == null) // support old versions with typo
			type = In.getEnum(json, "processTyp", ProcessType.class);
		return type;
	}

	private void addParameters(JsonObject json, Process p) {
		JsonArray parameters = In.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (JsonElement e : parameters) {
			if (!e.isJsonObject())
				continue;
			JsonObject o = e.getAsJsonObject();
			String refId = In.getString(o, "@id");
			ParameterImport pi = new ParameterImport(refId, conf);
			Parameter parameter = new Parameter();
			pi.mapFields(o, parameter);
			p.getParameters().add(parameter);
		}
	}

	private void addExchanges(JsonObject json, Process p) {
		JsonArray exchanges = In.getArray(json, "exchanges");
		if (exchanges == null || exchanges.size() == 0)
			return;
		int lastId = 0;
		for (JsonElement e : exchanges) {
			if (!e.isJsonObject())
				continue;
			JsonObject o = e.getAsJsonObject();
			Exchange ex = Exchanges.map(o, conf);
			if (ex.internalId == 0) {
				ex.internalId = ++p.lastInternalId;
			}
			lastId = Math.max(lastId, ex.internalId);
			exchangeMap.put(ex.internalId, ex);
			p.getExchanges().add(ex);
			boolean isRef = In.getBool(o, "quantitativeReference", false);
			if (isRef)
				p.setQuantitativeReference(ex);
		}
		p.lastInternalId = lastId + 1;
	}

	private void addSocialAspects(JsonObject json, Process p) {
		JsonArray aspects = In.getArray(json, "socialAspects");
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
		a.indicator = SocialIndicatorImport.run(In.getRefId(json, "socialIndicator"), conf);
		a.comment = In.getString(json, "comment");
		a.quality = In.getString(json, "quality");
		a.rawAmount = In.getString(json, "rawAmount");
		a.activityValue = In.getDouble(json, "activityValue", 0d);
		a.riskLevel = In.getEnum(json, "riskLevel", RiskLevel.class);
		a.source = SourceImport.run(In.getRefId(json, "source"), conf);
		return a;
	}

	private void addAllocationFactors(JsonObject json, Process p) {
		JsonArray factors = In.getArray(json, "allocationFactors");
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
		String productId = In.getRefId(json, "product");
		Flow product = FlowImport.run(productId, conf);
		if (product == null)
			return null;
		AllocationFactor factor = new AllocationFactor();
		factor.setProductId(product.getId());
		Integer exchangeId = null;
		JsonObject exchange = In.getObject(json, "exchange");
		if (exchange != null)
			exchangeId = In.getInt(exchange, "internalId", 0);
		if (exchangeId != null && exchangeId != 0)
			factor.setExchange(exchangeMap.get(exchangeId));
		factor.setValue(In.getDouble(json, "value", 1));
		factor.setAllocationType(In.getEnum(json, "allocationType", AllocationMethod.class));
		return factor;
	}

}