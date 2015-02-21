package org.openlca.jsonld.input;

import java.util.Objects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String refId;
	private EntityStore store;
	private Db db;

	private ProcessImport(String refId, EntityStore store, Db db) {
		this.refId = refId;
		this.store = store;
		this.db = db;
	}

	static Process run(String refId, EntityStore store, Db db) {
		return new ProcessImport(refId, store, db).run();
	}

	private Process run() {
		if (refId == null || store == null || db == null)
			return null;
		try {
			Process p = db.getProcess(refId);
			if (p != null)
				return p;
			JsonObject json = store.get(ModelType.PROCESS, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import source " + refId, e);
			return null;
		}
	}

	private Process map(JsonObject json) {
		if (json == null)
			return null;
		Process p = new Process();
		In.mapAtts(json, p);
		String catId = In.getRefId(json, "category");
		p.setCategory(CategoryImport.run(catId, store, db));
		mapProcessType(json, p);
		mapAllocationType(json, p);
		ProcessDocumentation doc = ProcessDocReader.read(json, store, db);
		p.setDocumentation(doc);
		String locId = In.getRefId(json, "location");
		if (locId != null)
			p.setLocation(LocationImport.run(locId, store, db));
		addExchanges(json, p);
		return db.put(p);
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
		String refExchange = In.getRefId(json, "quantitativeReference");
		JsonElement exchanges = json.get("exchanges");
		if (exchanges == null || !exchanges.isJsonArray())
			return;
		for (JsonElement e : exchanges.getAsJsonArray()) {
			if (!e.isJsonObject())
				continue;
			JsonObject o = e.getAsJsonObject();
			Exchange exchange = exchange(o);
			p.getExchanges().add(exchange);
			String id = In.getString(o, "@id");
			if (Objects.equals(refExchange, id))
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
		Flow flow = FlowImport.run(flowId, store, db);
		e.setFlow(flow);
		String unitId = In.getRefId(json, "unit");
		e.setUnit(db.getUnit(unitId));
		JsonElement factor = json.get("flowPropertyFactor");
		if (factor == null || !factor.isJsonObject())
			return;
		String propId = In.getRefId(factor.getAsJsonObject(), "flowProperty");
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