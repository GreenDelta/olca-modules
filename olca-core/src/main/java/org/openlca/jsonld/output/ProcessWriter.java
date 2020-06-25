package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;
import org.openlca.util.AllocationCleanup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.util.Processes;

class ProcessWriter extends Writer<Process> {

	private Process process;

	ProcessWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(Process p) {
		JsonObject obj = super.write(p);
		if (obj == null)
			return null;
		this.process = p;
		AllocationCleanup.on(p);
		Out.put(obj, "processType", p.processType, Out.REQUIRED_FIELD);
		Out.put(obj, "defaultAllocationMethod", p.defaultAllocationMethod);
		Out.put(obj, "infrastructureProcess", p.infrastructureProcess);
		Out.put(obj, "location", p.location, conf);
		Out.put(obj, "processDocumentation", Documentation.create(p, conf));
		Out.put(obj, "currency", p.currency, conf);
		Out.put(obj, "dqSystem", p.dqSystem, conf);
		Out.put(obj, "dqEntry", p.dqEntry);
		Out.put(obj, "exchangeDqSystem", p.exchangeDqSystem, conf);
		Out.put(obj, "socialDqSystem", p.socialDqSystem, conf);
		Out.put(obj, "lastInternalId", p.lastInternalId);
		mapParameters(obj);
		mapExchanges(obj);
		mapSocialAspects(obj);
		mapAllocationFactors(obj);
		GlobalParameters.sync(p, conf);
		return obj;
	}

	private void mapParameters(JsonObject json) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : process.parameters) {
			JsonObject obj = Writer.initJson();
			ParameterWriter.mapAttr(obj, p);
			parameters.add(obj);
		}
		Out.put(json, "parameters", parameters);
	}

	private void mapExchanges(JsonObject json) {
		var exchanges = conf.isLibraryExport
				? Processes.getProviderFlows(process)
				: process.exchanges;
		var array = new JsonArray();
		for (Exchange e : exchanges) {
			var obj = new JsonObject();
			boolean mapped = Exchanges.map(e, obj, conf);
			if (!mapped)
				continue;
			if (Objects.equals(process.quantitativeReference, e))
				Out.put(obj, "quantitativeReference", true);
			array.add(obj);
		}
		Out.put(json, "exchanges", array);
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
		for (AllocationFactor f : process.allocationFactors) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", AllocationFactor.class.getSimpleName());
			Out.put(obj, "allocationType", f.method, Out.REQUIRED_FIELD);
			if (f.method == AllocationMethod.CAUSAL) {
				Out.put(obj, "exchange", createExchangeRef(f.exchange), Out.REQUIRED_FIELD);
			}
			Out.put(obj, "product", findProduct(f.productId), conf, Out.REQUIRED_FIELD);
			Out.put(obj, "value", f.value);
			Out.put(obj, "formula", f.formula);
			factors.add(obj);
		}
		Out.put(json, "allocationFactors", factors);
	}

	private Flow findProduct(long id) {
		for (Exchange e : process.exchanges)
			if (e.flow.id == id)
				return e.flow;
		return null;
	}

	private JsonObject createExchangeRef(Exchange exchange) {
		if (exchange == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", Exchange.class.getSimpleName());
		Out.put(obj, "internalId", exchange.internalId);
		Out.put(obj, "flow", exchange.flow, conf);
		return obj;
	}

}
