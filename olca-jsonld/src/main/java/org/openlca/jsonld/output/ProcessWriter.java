package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialAspect;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProcessWriter extends Writer<Process> {

	private Process process;
	private Consumer<RootEntity> refFn;
	private Map<Long, String> idToRefId = new HashMap<>();
	private ExportConfig conf;

	ProcessWriter(ExportConfig conf) {
		this.conf = conf;
	}

	@Override
	JsonObject write(Process process, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(process, refFn);
		if (obj == null)
			return null;
		this.process = process;
		this.refFn = refFn;
		Out.put(obj, "processType", process.getProcessType());
		Out.put(obj, "defaultAllocationMethod",
				process.getDefaultAllocationMethod());
		Out.put(obj, "location", process.getLocation(), refFn);
		Out.put(obj, "processDocumentation",
				Documentation.create(process, refFn));
		Out.put(obj, "currency", process.currency, refFn);
		mapParameters(obj);
		mapExchanges(obj);
		mapSocialAspects(obj);
		mapAllocationFactors(obj);
		return obj;
	}

	private void mapParameters(JsonObject json) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : process.getParameters()) {
			JsonObject obj = new ParameterWriter().write(p, ref -> {
			});
			parameters.add(obj);
		}
		Out.put(json, "parameters", parameters);
	}

	private void mapExchanges(JsonObject json) {
		JsonArray exchanges = new JsonArray();
		for (Exchange e : process.getExchanges()) {
			JsonObject obj = new JsonObject();
			String id = Exchanges.map(e, process.getRefId(), obj, conf, refFn);
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
			Out.put(obj, "@type", "SocialAspect");
			Out.put(obj, "socialIndicator", a.indicator, refFn);
			Out.put(obj, "comment", a.comment);
			Out.put(obj, "quality", a.quality);
			Out.put(obj, "rawAmount", a.rawAmount);
			Out.put(obj, "activityValue", a.activityValue);
			Out.put(obj, "riskLevel", a.riskLevel);
			Out.put(obj, "source", a.source, refFn);
			aspects.add(obj);
		}
		Out.put(json, "socialAspects", aspects);
	}

	private void mapAllocationFactors(JsonObject json) {
		JsonArray factors = new JsonArray();
		for (AllocationFactor f : process.getAllocationFactors()) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "exchange", createExchangeRef(f.getExchange()));
			Out.put(obj, "product", findProduct(f.getProductId()), refFn);
			Out.put(obj, "value", f.getValue());
			Out.put(obj, "allocationType", f.getAllocationType());
			factors.add(obj);
		}
		Out.put(json, "allocationFactors", factors);
	}

	private Flow findProduct(long id) {
		for (Exchange e : process.getExchanges())
			if (e.getFlow().getId() == id)
				return e.getFlow();
		return null;
	}

	private JsonObject createExchangeRef(Exchange exchange) {
		if (exchange == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", Exchange.class.getSimpleName());
		Out.put(obj, "@id", idToRefId.get(exchange.getId()));
		return obj;
	}

}
