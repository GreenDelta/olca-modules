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
import org.openlca.core.database.ResultDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gnu.trove.map.hash.TLongLongHashMap;

class ProductSystemWriter extends Writer<ProductSystem> {

	private ProcessDao processDao;
	private FlowDao flowDao;
	private ProductSystem system;

	ProductSystemWriter(JsonExport exp) {
		super(exp);
		if (exp.db != null) {
			processDao = new ProcessDao(exp.db);
			flowDao = new FlowDao(exp.db);
		}
	}

	@Override
	JsonObject write(ProductSystem system) {
		JsonObject obj = super.write(system);
		if (obj == null)
			return null;

		this.system = system;
		TLongLongHashMap exchangeIDs = exchangeIDs(system);

		Json.put(obj, "referenceProcess", exp.handleRef(system.referenceProcess));

		// the reference exchange
		if (system.referenceExchange != null) {
			var eObj = new JsonObject();
			Json.put(eObj, "@type", "Exchange");
			Json.put(eObj, "internalId", exchangeIDs.get(system.referenceExchange.id));
			Json.put(eObj, "flow", exp.handleRef(system.referenceExchange.flow));
			Json.put(obj, "referenceExchange", eObj);
		}

		FlowProperty property = null;
		if (system.targetFlowPropertyFactor != null)
			property = system.targetFlowPropertyFactor.flowProperty;
		Json.put(obj, "targetFlowProperty", exp.handleRef(property));
		Json.put(obj, "targetUnit", Json.asRef(system.targetUnit));
		Json.put(obj, "targetAmount", system.targetAmount);

		// map the parameter redefinitions
		GlobalParameters.sync(system, exp);
		putParameterSets(obj, system.parameterSets);

		if (exp.db == null)
			return obj;
		Map<Long, CategorizedDescriptor> processMap = mapProcesses(obj);
		mapLinks(obj, processMap, exchangeIDs);
		return obj;
	}

	private void mapLinks(JsonObject json,
			Map<Long, CategorizedDescriptor> processMap,
			TLongLongHashMap exchangeIDs) {
		JsonArray links = new JsonArray();
		Map<Long, FlowDescriptor> flows = getFlows();
		for (ProcessLink link : system.processLinks) {
			JsonObject obj = new JsonObject();
			link.

			Json.put(obj, "provider", exp.handleRef(ModelType.PROCESS, link.providerId));
			Json.put(obj, "flow",
				exp.handleRef(ModelType.FLOW, flows.get(link.flowId), conf));
			JsonObject process = References
					.create(processMap.get(link.processId), conf);
			Json.put(obj, "process", process);
			JsonObject eObj = new JsonObject();
			Json.put(eObj, "@type", "Exchange");
			Json.put(eObj, "internalId", exchangeIDs.get(link.exchangeId));
			Json.put(obj, "exchange", eObj);
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
		var processes = processDao.descriptorMap();
		var systems = new ProductSystemDao(conf.db).descriptorMap();
		var results = new ResultDao(conf.db).descriptorMap();

		var map = new HashMap<Long, CategorizedDescriptor>();
		var array = new JsonArray();
		for (var id : system.processes) {
			CategorizedDescriptor d = processes.get(id);
			if (d == null) {
				d = systems.get(id);
				if (d == null) {
					d = results.get(id);
				}
			}
			if (d == null)
				continue;
			map.put(id, d);
			var ref = conf.exportReferences
					? References.create(d.type, d.id, conf, false)
					: References.create(d, conf);
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

	private void putParameterSets(JsonObject obj, List<ParameterRedefSet> sets) {
		if (sets.isEmpty())
			return;
		var jsonSets = new JsonArray();
		for (var set : sets) {
			var jsonSet = new JsonObject();
			jsonSets.add(jsonSet);
			Json.put(jsonSet, "name", set.name);
			Json.put(jsonSet, "description", set.description);
			Json.put(jsonSet, "isBaseline", set.isBaseline);
			if (!set.parameters.isEmpty()) {
				var params = ParameterRedefs.map(set.parameters, exp);

				Out.put(jsonSet, "parameters", params);
			}
		}
		Json.put(obj, "parameterSets", jsonSets);
	}

	@Override
	boolean isExportExternalFiles() {
		// Product system files are using local ids, this must be changed first,
		// otherwise this leads to problems after import
		return false;
	}

}
