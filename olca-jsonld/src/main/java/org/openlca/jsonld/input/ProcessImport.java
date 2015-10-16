package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProcessImport extends BaseImport<Process> {

	private Logger log = LoggerFactory.getLogger(getClass());

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
		p.setId(id);
		In.mapAtts(json, p);
		String catId = In.getRefId(json, "category");
		p.setCategory(CategoryImport.run(catId, conf));
		mapProcessType(json, p);
		mapAllocationType(json, p);
		ProcessDocumentation doc = ProcessDocReader.read(json, conf);
		p.setDocumentation(doc);
		String locId = In.getRefId(json, "location");
		if (locId != null)
			p.setLocation(LocationImport.run(locId, conf));
		addExchanges(json, p);
		return conf.db.put(p);
	}

	private void mapProcessType(JsonObject json, Process p) {
		String type = In.getString(json, "processTyp");
		if (type == null)
			return;
		try {
			p.setProcessType(ProcessType.valueOf(type));
		} catch (Exception e) {
			log.warn("unknown process type " + type, e);
		}
	}

	private void mapAllocationType(JsonObject json, Process p) {
		String type = In.getString(json, "defaultAllocationMethod");
		if (type == null)
			return;
		switch (type) {
		case "CAUSAL_ALLOCATION":
			p.setDefaultAllocationMethod(AllocationMethod.CAUSAL);
			break;
		case "ECONOMIC_ALLOCATION":
			p.setDefaultAllocationMethod(AllocationMethod.ECONOMIC);
			break;
		case "PHYSICAL_ALLOCATION":
			p.setDefaultAllocationMethod(AllocationMethod.PHYSICAL);
			break;
		default:
			log.warn("unknown allocation type " + type);
		}
	}

	private void addExchanges(JsonObject json, Process p) {
		JsonElement exchanges = json.get("exchanges");
		if (exchanges == null || !exchanges.isJsonArray())
			return;
		for (JsonElement e : exchanges.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			JsonObject o = e.getAsJsonObject();
			Exchange exchange = exchange(o);
			p.getExchanges().add(exchange);
			boolean isRef = In.getBool(o, "quantitativeReference", false);
			if (isRef)
				p.setQuantitativeReference(exchange);
		}
	}

	private Exchange exchange(JsonObject json) {
		Exchange e = new Exchange();
		e.setAvoidedProduct(In.getBool(json, "avoidedProduct", false));
		e.setInput(In.getBool(json, "input", false));
		e.setBaseUncertainty(In.getDouble(json, "baseUncertainty", 0));
		e.setAmountValue(In.getDouble(json, "amount", 0));
		// TODO: import formulas when parameters are imported
		// e.setAmountFormula(In.getString(json, "amountFormula"));
		e.setPedigreeUncertainty(In.getString(json, "pedigreeUncertainty"));
		JsonElement u = json.get("uncertainty");
		if (u != null && u.isJsonObject())
			e.setUncertainty(Uncertainties.read(u.getAsJsonObject()));
		addExchangeRefs(json, e);
		return e;
	}

	private void addExchangeRefs(JsonObject json, Exchange e) {
		String flowId = In.getRefId(json, "flow");
		Flow flow = FlowImport.run(flowId, conf);
		e.setFlow(flow);
		String unitId = In.getRefId(json, "unit");
		e.setUnit(conf.db.getUnit(unitId));
		String propId = In.getRefId(json, "flowProperty");
		for (FlowPropertyFactor f : flow.getFlowPropertyFactors()) {
			FlowProperty prop = f.getFlowProperty();
			if (prop == null)
				continue;
			if (Objects.equals(propId, prop.getRefId())) {
				e.setFlowPropertyFactor(f);
				break;
			}
		}
	}

}