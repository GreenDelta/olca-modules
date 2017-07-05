package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProcessWriter extends Writer<Process> {

	private Process process;
	private Map<Long, String> idToRefId = new HashMap<>();

	ProcessWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(Process p) {
		JsonObject obj = super.write(p);
		if (obj == null)
			return null;
		this.process = p;
		Out.put(obj, "processType", p.getProcessType(), Out.REQUIRED_FIELD);
		Out.put(obj, "defaultAllocationMethod", p.getDefaultAllocationMethod());
		Out.put(obj, "infrastructureProcess", p.isInfrastructureProcess());
		Out.put(obj, "location", p.getLocation(), conf);
		Out.put(obj, "processDocumentation", Documentation.create(p, conf));
		Out.put(obj, "currency", p.currency, conf);
		Out.put(obj, "dqSystem", p.dqSystem, conf);
		Out.put(obj, "dqEntry", p.dqEntry);
		Out.put(obj, "exchangeDqSystem", p.exchangeDqSystem, conf);
		Out.put(obj, "socialDqSystem", p.socialDqSystem, conf);
		mapParameters(obj);
		mapExchanges(obj);
		mapSocialAspects(obj);
		mapAllocationFactors(obj);
		ParameterReferences.writeReferencedParameters(p, conf);
		return obj;
	}

	private void mapParameters(JsonObject json) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : process.getParameters()) {
			JsonObject obj = Writer.initJson();
			ParameterWriter.mapAttr(obj, p);
			parameters.add(obj);
		}
		Out.put(json, "parameters", parameters);
	}

	private void mapExchanges(JsonObject json) {
		JsonArray exchanges = new JsonArray();
		for (Exchange e : process.getExchanges()) {
			JsonObject obj = new JsonObject();
			String id = Exchanges.map(e, process.getRefId(), obj, conf);
			if (id == null)
				continue;
			idToRefId.put(e.getId(), id);
			if (Objects.equals(process.getQuantitativeReference(), e))
				Out.put(obj, "quantitativeReference", true);
			exchanges.add(obj);
		}
		Out.put(json, "exchanges", exchanges);
	}

	private void mapSocialAspects(JsonObject json) {
		JsonArray aspects = new JsonArray();
		for (SocialAspect a : process.socialAspects) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", SocialAspect.class.getSimpleName());
			Out.put(obj, "socialIndicator", a.indicator, conf, Out.REQUIRED_FIELD);
			Out.put(obj, "comment", a.comment);
			Out.put(obj, "quality", a.quality);
			Out.put(obj, "rawAmount", a.rawAmount);
			Out.put(obj, "activityValue", a.activityValue);
			Out.put(obj, "riskLevel", a.riskLevel);
			Out.put(obj, "source", a.source, conf);
			aspects.add(obj);
		}
		Out.put(json, "socialAspects", aspects);
	}

	private void mapAllocationFactors(JsonObject json) {
		JsonArray factors = new JsonArray();
		for (AllocationFactor f : process.getAllocationFactors()) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", AllocationFactor.class.getSimpleName());
			Out.put(obj, "allocationType", f.getAllocationType(), Out.REQUIRED_FIELD);
			if (f.getAllocationType() == AllocationMethod.CAUSAL) {
				Out.put(obj, "exchange", createExchangeRef(f.getExchange()), Out.REQUIRED_FIELD);
			}
			Out.put(obj, "product", findProduct(f.getProductId()), conf, Out.REQUIRED_FIELD);
			Out.put(obj, "value", f.getValue());
			factors.add(obj);
		}
		Out.put(json, "allocationFactors", factors);
	}

	private Flow findProduct(long id) {
		for (Exchange e : process.getExchanges())
			if (e.flow.getId() == id)
				return e.flow;
		return null;
	}

	private JsonObject createExchangeRef(Exchange exchange) {
		if (exchange == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", Exchange.class.getSimpleName());
		Out.put(obj, "@id", idToRefId.get(exchange.getId()));
		Out.put(obj, "flow", exchange.flow, conf);
		return obj;
	}

}
