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
		} else {
			idMap = new RefIdMap<>();
		}
	}

	@Override
	JsonObject write(ProductSystem system) {
		JsonObject obj = super.write(system);
		if (obj == null)
			return null;
		this.system = system;
		Out.put(obj, "referenceProcess", system.getReferenceProcess(), conf, Out.REQUIRED_FIELD);
		String processRefId = null;
		if (system.getReferenceProcess() != null)
			processRefId = system.getReferenceProcess().getRefId();
		JsonObject eObj = getExchange(processRefId, system.getReferenceExchange());
		Out.put(obj, "referenceExchange", eObj, Out.REQUIRED_FIELD);
		FlowProperty property = null;
		if (system.getTargetFlowPropertyFactor() != null)
			property = system.getTargetFlowPropertyFactor().getFlowProperty();
		Out.put(obj, "targetFlowProperty", property, conf, Out.REQUIRED_FIELD);
		Out.put(obj, "targetUnit", system.getTargetUnit(), conf, Out.REQUIRED_FIELD);
		Out.put(obj, "targetAmount", system.getTargetAmount());
		ParameterRedefs.map(obj, system.getParameterRedefs(), conf.db, conf, (type, id) -> processRef(id));
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
			Out.put(obj, "provider", processRef(link.providerId), Out.REQUIRED_FIELD);
			Out.put(obj, "flow", References.create(ModelType.FLOW, link.flowId, conf, true), Out.REQUIRED_FIELD);
			JsonObject process = processRef(link.processId);
			Out.put(obj, "process", process, Out.REQUIRED_FIELD);
			JsonObject exchange = null;
			if (exchangeDao != null && process != null && process.has("@id")) {
				Exchange e = exchangeDao.getForId(link.exchangeId);
				exchange = getExchange(process.get("@id").getAsString(), e);
			}
			Out.put(obj, "exchange", exchange, Out.REQUIRED_FIELD);
			links.add(obj);
		}
		Out.put(json, "processLinks", links);
	}

	private void mapProcesses(JsonObject json) {
		JsonArray processes = new JsonArray();
		for (Long pId : system.getProcesses()) {
			JsonObject ref = processRef(pId);
			if (ref == null)
				continue;
			processes.add(ref);
		}
		Out.put(json, "processes", processes);
	}

	private JsonObject processRef(Long id) {
		if (id == null)
			return null;
		return References.create(ModelType.PROCESS, id, conf, true);
	}

	private JsonObject getExchange(String processRefId, Exchange e) {
		if (e == null || processRefId == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", "Exchange");
		String providerId = idMap.get(Process.class, e.defaultProviderId);
		String id = ExchangeKey.get(processRefId, providerId, e);
		Out.put(obj, "@id", id);
		Out.put(obj, "flow", createRef(e.flow), Out.REQUIRED_FIELD);
		Out.put(obj, "unit", createRef(e.unit), Out.REQUIRED_FIELD);
		Out.put(obj, "amount", e.amount);
		Out.put(obj, "input", e.isInput);
		if (providerId != null) {
			JsonObject providerRef = new JsonObject();
			Out.put(providerRef, "@type", "Process");
			Out.put(providerRef, "@id", providerId);
			Out.put(obj, "defaultProvider", providerRef);
		}
		return obj;
	}

	private JsonObject createRef(RootEntity ref) {
		if (ref == null || ref.getRefId() == null)
			return null;
		JsonObject refObj = new JsonObject();
		Out.put(refObj, "@type", ref.getClass().getSimpleName());
		Out.put(refObj, "@id", ref.getRefId());
		Out.put(refObj, "name", ref.getName());
		return refObj;
	}

	@Override
	boolean isExportExternalFiles() {
		// Product system files are using local ids, this must be changed first,
		// otherwise this leads to problems after import
		return false;
	}

}
