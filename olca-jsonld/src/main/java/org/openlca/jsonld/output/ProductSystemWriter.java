package org.openlca.jsonld.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.jsonld.ExchangeKey;
import org.openlca.util.RefIdMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProductSystemWriter extends Writer<ProductSystem> {

	private ProcessDao processDao;
	private FlowDao flowDao;
	private BaseDao<Exchange> exchangeDao;
	private RefIdMap<Long, String> idMap;
	private ProductSystem system;

	ProductSystemWriter(ExportConfig conf) {
		super(conf);
		if (conf.db != null) {
			processDao = new ProcessDao(conf.db);
			flowDao = new FlowDao(conf.db);
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
		if (conf.db == null)
			return obj;
		Map<Long, ProcessDescriptor> processMap = mapProcesses(obj);
		mapLinks(obj, processMap);
		ParameterRedefs.map(obj, system.getParameterRedefs(), conf.db, conf,
				(type, id) -> References.create(processMap.get(id)));
		ParameterReferences.writeReferencedParameters(system, conf);
		return obj;
	}

	private void mapLinks(JsonObject json, Map<Long, ProcessDescriptor> processMap) {
		JsonArray links = new JsonArray();
		Map<Long, FlowDescriptor> flowMap = getFlows();
		Stack<ProcessLink> remaining = new Stack<>();
		remaining.addAll(system.getProcessLinks());
		while (!remaining.isEmpty()) {
			List<ProcessLink> next = new ArrayList<>();
			while (!remaining.isEmpty() && next.size() < 1000) {
				next.add(remaining.pop());
			}
			Map<Long, Exchange> exchangeMap = getExchanges(next);
			for (ProcessLink link : next) {
				JsonObject obj = new JsonObject();
				Out.put(obj, "@type", "ProcessLink");
				JsonObject provider = References.create(processMap.get(link.providerId));
				Out.put(obj, "provider", provider, Out.REQUIRED_FIELD);
				Out.put(obj, "flow", References.create(flowMap.get(link.flowId)), Out.REQUIRED_FIELD);
				JsonObject process = References.create(processMap.get(link.processId));
				Out.put(obj, "process", process, Out.REQUIRED_FIELD);
				JsonObject exchange = null;
				if (process != null && process.has("@id")) {
					Exchange e = exchangeMap.get(link.exchangeId);
					exchange = getExchange(process.get("@id").getAsString(), e);
				}
				Out.put(obj, "exchange", exchange, Out.REQUIRED_FIELD);
				links.add(obj);
			}
		}
		Out.put(json, "processLinks", links);
	}

	private Map<Long, Exchange> getExchanges(List<ProcessLink> links) {
		Set<Long> exchangeIds = new HashSet<>();
		for (ProcessLink link : links) {
			exchangeIds.add(link.exchangeId);
		}
		List<Exchange> exchanges = exchangeDao.getForIds(exchangeIds);
		Map<Long, Exchange> exchangeMap = new HashMap<>();
		for (Exchange e : exchanges) {
			exchangeMap.put(e.getId(), e);
		}
		return exchangeMap;
	}

	private Map<Long, FlowDescriptor> getFlows() {
		Set<Long> flowIds = new HashSet<>();
		for (ProcessLink link : system.getProcessLinks()) {
			flowIds.add(link.flowId);
		}
		List<FlowDescriptor> flows = flowDao.getDescriptors(flowIds);
		Map<Long, FlowDescriptor> flowMap = new HashMap<>();
		for (FlowDescriptor f : flows) {
			flowMap.put(f.getId(), f);
		}
		return flowMap;
	}

	private Map<Long, ProcessDescriptor> mapProcesses(JsonObject json) {
		JsonArray processes = new JsonArray();
		Set<Long> pIds = new HashSet<>(system.getProcesses());
		List<ProcessDescriptor> descriptors = processDao.getDescriptors(pIds);
		Map<Long, ProcessDescriptor> map = new HashMap<>();
		for (ProcessDescriptor descriptor : descriptors) {
			map.put(descriptor.getId(), descriptor);
			JsonObject ref = References.create(descriptor);
			if (ref == null)
				continue;
			processes.add(ref);
		}
		Out.put(json, "processes", processes);
		return map;
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
