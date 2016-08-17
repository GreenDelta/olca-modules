package org.openlca.jsonld.output;

import org.openlca.core.database.BaseDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.ExchangeKey;
import org.openlca.util.RefIdMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProductSystemWriter extends Writer<ProductSystem> {

	private BaseDao<Exchange> exchangeDao;
	private RefIdMap<Long, String> idMap;
	private ProductSystem system;

	ProductSystemWriter(ExportConfig conf) {
		super(conf);
		if (conf.db != null) {
			exchangeDao = new BaseDao<>(Exchange.class, conf.db);
			idMap = RefIdMap.internalToRef(conf.db, Process.class);
		}
	}

	@Override
	JsonObject write(ProductSystem system) {
		JsonObject obj = super.write(system);
		if (obj == null)
			return null;
		this.system = system;
		Out.put(obj, "referenceProcess", system.getReferenceProcess(), conf);
		String processRefId = null;
		if (system.getReferenceProcess() != null)
			processRefId = system.getReferenceProcess().getRefId();
		JsonObject eObj = getExchange(processRefId, system.getReferenceExchange());
		Out.put(obj, "referenceExchange", eObj);
		FlowProperty property = null;
		if (system.getTargetFlowPropertyFactor() != null)
			property = system.getTargetFlowPropertyFactor().getFlowProperty();
		Out.put(obj, "targetFlowProperty", property, conf);
		Out.put(obj, "targetUnit", system.getTargetUnit(), conf);
		Out.put(obj, "targetAmount", system.getTargetAmount());
		ParameterRedefs.map(obj, system.getParameterRedefs(), conf.db, conf, (
				type, id) -> processRef(id));
		if (conf.db == null)
			return obj;
		mapProcesses(obj);
		mapLinks(obj);
		ParameterReferences.writeReferencedParameters(system, conf);
		return obj;
	}

	private void mapLinks(JsonObject json) {
		JsonArray links = new JsonArray();
		for (ProcessLink link : system.getProcessLinks()) {
			JsonObject obj = new JsonObject();
			links.add(obj);
			Out.put(obj, "@type", "ProcessLink");
			Out.put(obj, "provider", processRef(link.providerId));
			Out.put(obj, "flow", References.create(
					ModelType.FLOW, link.flowId, conf, true));
			JsonObject process = processRef(link.processId);
			Out.put(obj, "process", process);
			if (exchangeDao == null)
				continue;
			Exchange e = exchangeDao.getForId(link.exchangeId);
			JsonObject exchange = getExchange(
					process.get("@id").getAsString(), e);
			Out.put(obj, "exchange", exchange);
		}
		Out.put(json, "processLinks", links);
	}

	private void mapProcesses(JsonObject json) {
		JsonArray processes = new JsonArray();
		for (Long pId : system.getProcesses())
			processes.add(processRef(pId));
		Out.put(json, "processes", processes);
	}

	private JsonObject processRef(Long id) {
		if (id == null)
			return null;
		return References.create(ModelType.PROCESS, id, conf, true);
	}

	private JsonObject getExchange(String processRefId, Exchange e) {
		if (e == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", "Exchange");
		String providerId = idMap.get(Process.class, e.getDefaultProviderId());
		String id = ExchangeKey.get(processRefId, providerId, e);
		Out.put(obj, "@id", id);
		addRef(obj, "flow", e.getFlow());
		addRef(obj, "unit", e.getUnit());
		Out.put(obj, "amount", e.getAmountValue());
		if (providerId != null) {
			JsonObject providerRef = new JsonObject();
			Out.put(providerRef, "@type", "Process");
			Out.put(providerRef, "@id", providerId);
			Out.put(obj, "defaultProvider", providerRef);
		}
		return obj;
	}

	private void addRef(JsonObject obj, String key, RootEntity ref) {
		if (ref == null || ref.getRefId() == null)
			return;
		JsonObject refObj = new JsonObject();
		obj.add(key, refObj);
		Out.put(refObj, "@type", ref.getClass().getSimpleName());
		Out.put(refObj, "@id", ref.getRefId());
	}

	@Override
	boolean isExportExternalFiles() {
		// Product system files are using local ids, this must be changed first,
		// otherwise this leads to problems after import
		return false;
	}

}
