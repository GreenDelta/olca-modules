package org.openlca.jsonld.output;

import java.util.List;

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
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gnu.trove.map.hash.TLongLongHashMap;

class ProductSystemWriter extends Writer<ProductSystem> {

	private ProductSystem system;

	ProductSystemWriter(JsonExport exp) {
		super(exp);
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
		mapProcesses(obj);
		mapLinks(obj, exchangeIDs);
		return obj;
	}

	private void mapLinks(JsonObject json, TLongLongHashMap exchangeIDs) {
		var array = new JsonArray();
		for (var link : system.processLinks) {
			var obj = new JsonObject();
			var providerType = providerTypeOf(link);
			Json.put(obj, "provider", exp.handleRef(providerType, link.providerId));
			Json.put(obj, "flow",	exp.handleRef(ModelType.FLOW, link.flowId));
			Json.put(obj, "process", exp.handleRef(ModelType.PROCESS, link.processId));
			var eObj = new JsonObject();
			Json.put(eObj, "@type", "Exchange");
			Json.put(eObj, "internalId", exchangeIDs.get(link.exchangeId));
			Json.put(obj, "exchange", eObj);
			array.add(obj);
		}
		Json.put(json, "processLinks", array);
	}

	private ModelType providerTypeOf(ProcessLink link) {
		if (link.hasSubSystemProvider())
			return ModelType.PRODUCT_SYSTEM;
		if (link.hasResultProvider())
			return ModelType.RESULT;
		return ModelType.PROCESS;
	}

	private void mapProcesses(JsonObject json) {
		var processes = new ProcessDao(exp.db).descriptorMap();
		var systems = new ProductSystemDao(exp.db).descriptorMap();
		var results = new ResultDao(exp.db).descriptorMap();
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
			var ref = exp.handleRef(d.type, d.id);
			if (ref == null)
				continue;
			array.add(ref);
		}
		Json.put(json, "processes", array);
	}

	/**
	 * Creates a map exchangeID -> internalID of the exchanges used in the
	 * product system links.
	 */
	private TLongLongHashMap exchangeIDs(ProductSystem system) {
		var map = new TLongLongHashMap();
		if (system.referenceExchange != null) {
			map.put(system.referenceExchange.id, -1);
		}
		for (ProcessLink link : system.processLinks) {
			map.put(link.exchangeId, -1L);
		}
		try {
			String sql = "select id, internal_id from tbl_exchanges";
			NativeSql.on(exp.db).query(sql, r -> {
				long id = r.getLong(1);
				long internal = r.getLong(2);
				if (map.containsKey(id)) {
					map.put(id, internal);
				}
				return true;
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to query exchange IDs", e);
		}
		return map;
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
				var params = Util.mapRedefs(set.parameters, exp);
				Json.put(jsonSet, "parameters", params);
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
