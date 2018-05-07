package org.openlca.jsonld.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.openlca.core.database.ExchangeDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProductSystemWriter extends Writer<ProductSystem> {

	private ProcessDao processDao;
	private FlowDao flowDao;
	private ExchangeDao exchangeDao;
	private ProductSystem system;

	ProductSystemWriter(ExportConfig conf) {
		super(conf);
		if (conf.db != null) {
			processDao = new ProcessDao(conf.db);
			flowDao = new FlowDao(conf.db);
			exchangeDao = new ExchangeDao(conf.db);
		}
	}

	@Override
	JsonObject write(ProductSystem system) {
		JsonObject obj = super.write(system);
		if (obj == null)
			return null;
		this.system = system;
		Out.put(obj, "referenceProcess", system.referenceProcess, conf, Out.REQUIRED_FIELD);
		JsonObject eObj = mapExchange(system.referenceExchange);
		Out.put(obj, "referenceExchange", eObj, Out.REQUIRED_FIELD);
		FlowProperty property = null;
		if (system.targetFlowPropertyFactor != null)
			property = system.targetFlowPropertyFactor.getFlowProperty();
		Out.put(obj, "targetFlowProperty", property, conf, Out.REQUIRED_FIELD);
		Out.put(obj, "targetUnit", system.targetUnit, conf, Out.REQUIRED_FIELD);
		Out.put(obj, "targetAmount", system.targetAmount);
		putInventory(obj, system.inventory);
		if (conf.db == null)
			return obj;
		Map<Long, ProcessDescriptor> processMap = mapProcesses(obj);
		mapLinks(obj, processMap);
		ParameterRedefs.map(obj, system.parameterRedefs, conf.db, conf,
				(type, id) -> References.create(processMap.get(id)));
		ParameterReferences.writeReferencedParameters(system, conf);
		return obj;
	}

	private void mapLinks(JsonObject json, Map<Long, ProcessDescriptor> processMap) {
		JsonArray links = new JsonArray();
		Map<Long, FlowDescriptor> flowMap = getFlows();
		Stack<ProcessLink> remaining = new Stack<>();
		remaining.addAll(system.processLinks);
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
				Exchange e = exchangeMap.get(link.exchangeId);
				JsonObject exchange = mapExchange(e);
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
		for (ProcessLink link : system.processLinks) {
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
		Set<Long> pIds = new HashSet<>(system.processes);
		List<ProcessDescriptor> descriptors = processDao.getDescriptors(pIds);
		Map<Long, ProcessDescriptor> map = new HashMap<>();
		for (ProcessDescriptor descriptor : descriptors) {
			map.put(descriptor.getId(), descriptor);
			JsonObject ref = null;
			if (conf.exportReferences) {
				ref = References.create(ModelType.PROCESS, descriptor.getId(), conf, false);
			} else {
				ref = References.create(descriptor);
			}
			if (ref == null)
				continue;
			processes.add(ref);
		}
		Out.put(json, "processes", processes);
		return map;
	}

	private JsonObject mapExchange(Exchange e) {
		if (e == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", "Exchange");
		Out.put(obj, "internalId", e.internalId);
		if (e.flow == null)
			return obj;
		Out.put(obj, "name", e.flow.getName());
		return obj;
	}

	private void putInventory(JsonObject obj, List<Exchange> inventory) {
		if (inventory.isEmpty())
			return;
		JsonArray inv = new JsonArray();
		for (Exchange exchange : inventory) {
			JsonObject eObj = new JsonObject();
			boolean mapped = Exchanges.map(exchange, eObj, conf);
			if (!mapped)
				continue;
			inv.add(eObj);
		}
		Out.put(obj, "inventory", inv);
	}

	@Override
	boolean isExportExternalFiles() {
		// Product system files are using local ids, this must be changed first,
		// otherwise this leads to problems after import
		return false;
	}

}
