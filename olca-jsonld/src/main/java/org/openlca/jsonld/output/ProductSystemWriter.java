package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

class ProductSystemWriter extends Writer<ProductSystem> {

	private ProcessDao processDao;
	private FlowDao flowDao;
	private ProductSystem system;

	ProductSystemWriter(ExportConfig conf) {
		super(conf);
		if (conf.db != null) {
			processDao = new ProcessDao(conf.db);
			flowDao = new FlowDao(conf.db);
		}
	}

	@Override
	JsonObject write(ProductSystem system) {
		JsonObject obj = super.write(system);
		if (obj == null)
			return null;

		this.system = system;
		TLongLongHashMap exchangeIDs = exchangeIDs(system);

		Out.put(obj, "referenceProcess", system.referenceProcess,
				conf, Out.REQUIRED_FIELD);

		// the reference exchange
		if (system.referenceExchange != null) {
			JsonObject eObj = new JsonObject();
			Out.put(eObj, "@type", "Exchange");
			Out.put(eObj, "internalId", exchangeIDs.get(
					system.referenceExchange.id));
			Out.put(obj, "referenceExchange", eObj,
					Out.REQUIRED_FIELD);
		}

		FlowProperty property = null;
		if (system.targetFlowPropertyFactor != null)
			property = system.targetFlowPropertyFactor.flowProperty;
		Out.put(obj, "targetFlowProperty", property, conf, Out.REQUIRED_FIELD);
		Out.put(obj, "targetUnit", system.targetUnit, conf, Out.REQUIRED_FIELD);
		Out.put(obj, "targetAmount", system.targetAmount);
		putInventory(obj, system.inventory);
		if (conf.db == null)
			return obj;

		Map<Long, CategorizedDescriptor> processMap = mapProcesses(obj);
		mapLinks(obj, processMap, exchangeIDs);
		ParameterRedefs.map(obj, system.parameterRedefs, conf.db, conf,
				(type, id) -> References.create(processMap.get(id), conf));
		ParameterReferences.writeReferencedParameters(system, conf);
		return obj;
	}

	private void mapLinks(JsonObject json,
			Map<Long, CategorizedDescriptor> processMap,
			TLongLongHashMap exchangeIDs) {
		JsonArray links = new JsonArray();
		Map<Long, FlowDescriptor> flows = getFlows();
		for (ProcessLink link : system.processLinks) {
			JsonObject obj = new JsonObject();
			Out.put(obj, "@type", "ProcessLink");
			JsonObject provider = References
					.create(processMap.get(link.providerId), conf);
			Out.put(obj, "provider", provider, Out.REQUIRED_FIELD);
			Out.put(obj, "isSystemLink", link.isSystemLink);
			Out.put(obj, "flow",
					References.create(flows.get(link.flowId), conf),
					Out.REQUIRED_FIELD);
			JsonObject process = References
					.create(processMap.get(link.processId), conf);
			Out.put(obj, "process", process, Out.REQUIRED_FIELD);
			JsonObject eObj = new JsonObject();
			Out.put(eObj, "@type", "Exchange");
			Out.put(eObj, "internalId", exchangeIDs.get(link.exchangeId));
			Out.put(obj, "exchange", eObj, Out.REQUIRED_FIELD);
			links.add(obj);
		}
		Out.put(json, "processLinks", links);
	}

	private Map<Long, FlowDescriptor> getFlows() {
		Set<Long> flowIds = new HashSet<>();
		for (ProcessLink link : system.processLinks) {
			flowIds.add(link.flowId);
		}
		List<FlowDescriptor> flows = flowDao.getDescriptors(flowIds);
		Map<Long, FlowDescriptor> flowMap = new HashMap<>();
		for (FlowDescriptor f : flows) {
			flowMap.put(f.id, f);
		}
		return flowMap;
	}

	private Map<Long, CategorizedDescriptor> mapProcesses(JsonObject json) {
		TLongObjectHashMap<ProcessDescriptor> processes = processDao
				.descriptorMap();
		TLongObjectHashMap<ProductSystemDescriptor> systems = new ProductSystemDao(
				conf.db).descriptorMap();
		Map<Long, CategorizedDescriptor> map = new HashMap<>();
		JsonArray array = new JsonArray();
		for (Long id : system.processes) {
			CategorizedDescriptor d = processes.get(id);
			if (d == null) {
				d = systems.get(id);
			}
			if (d == null)
				continue;
			map.put(id, d);
			JsonObject ref = null;
			if (conf.exportReferences) {
				ref = References.create(d.type, d.id, conf, false);
			} else {
				ref = References.create(d, conf);
			}
			if (ref == null)
				continue;
			array.add(ref);
		}
		Out.put(json, "processes", array);
		return map;
	}

	/**
	 * Creates a map exchangeID -> internalID of the exchanges used in the
	 * product system links.
	 */
	private TLongLongHashMap exchangeIDs(ProductSystem system) {
		TLongLongHashMap m = new TLongLongHashMap();
		if (system.referenceExchange != null) {
			m.put(system.referenceExchange.id, -1);
		}
		for (ProcessLink link : system.processLinks) {
			m.put(link.exchangeId, -1L);
		}
		try {
			String sql = "select id, internal_id from tbl_exchanges";
			NativeSql.on(conf.db).query(sql, r -> {
				long id = r.getLong(1);
				long internal = r.getLong(2);
				if (m.containsKey(id)) {
					m.put(id, internal);
				}
				return true;
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to query exchange IDs", e);
		}
		return m;
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
