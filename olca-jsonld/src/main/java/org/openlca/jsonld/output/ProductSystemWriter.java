package org.openlca.jsonld.output;

import org.openlca.core.database.BaseDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.ExchangeKey;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProductSystemWriter extends Writer<ProductSystem> {

	private BaseDao<Exchange> exchangeDao;
	private ProductSystem system;

	ProductSystemWriter(ExportConfig conf) {
		super(conf);
		if (conf.db != null) {
			exchangeDao = new BaseDao<>(Exchange.class, conf.db);
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
		JsonObject eObj = exchangeRef(processRefId, system.getReferenceExchange());
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
			Out.put(obj, "@type", "ProcessLink");
			Out.put(obj, "provider", processRef(link.providerId));
			Out.put(obj, "flow", References.create(
					ModelType.FLOW, link.flowId, conf, true));
			JsonObject process = processRef(link.processId);
			Out.put(obj, "process", process);
			JsonObject exchange = getExchange(link,
					process.get("@id").getAsString());
			Out.put(obj, "exchange", exchange);
			links.add(obj);
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

	private JsonObject exchangeRef(String pRefId, Exchange e) {
		if (e == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", Exchange.class.getSimpleName());
		String id = ExchangeKey.get(pRefId, getProviderRefId(e), e);
		Out.put(obj, "@id", id);
		return obj;
	}

	private JsonObject getExchange(ProcessLink link, String processRefId) {
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", Exchange.class.getSimpleName());
		if (exchangeDao == null)
			return obj;
		Exchange e = exchangeDao.getForId(link.exchangeId);
		if (e == null)
			return obj;
		String id = ExchangeKey.get(processRefId, getProviderRefId(e), e);
		Out.put(obj, "@id", id);
		return obj;
	}

	private String getProviderRefId(Exchange e) {
		JsonObject provider = null;
		Long pId = e.getDefaultProviderId();
		provider = References.create(ModelType.PROCESS, pId, conf,
				conf.exportProviders);
		if (provider == null)
			return null;
		return provider.get("@id").getAsString();
	}

	@Override
	boolean isExportExternalFiles() {
		// Product system files are using local ids, this must be changed first,
		// otherwise this leads to problems after import
		return false;
	}

}
