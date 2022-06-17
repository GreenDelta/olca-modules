package org.openlca.jsonld.output;

import java.util.List;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gnu.trove.map.hash.TLongLongHashMap;

record ProductSystemWriter(JsonExport exp) implements Writer<ProductSystem> {

	@Override
	public JsonObject write(ProductSystem sys) {
		var obj = Writer.init(sys);
		Json.put(obj, "refProcess", exp.handleRef(sys.referenceProcess));

		// the reference exchange
		var exchangeIDs = exchangeIDs(sys);
		if (sys.referenceExchange != null) {
			var eObj = new JsonObject();
			Json.put(eObj, "@type", "Exchange");
			Json.put(eObj, "internalId", exchangeIDs.get(sys.referenceExchange.id));
			Json.put(eObj, "flow", exp.handleRef(sys.referenceExchange.flow));
			Json.put(obj, "refExchange", eObj);
		}

		FlowProperty property = null;
		if (sys.targetFlowPropertyFactor != null)
			property = sys.targetFlowPropertyFactor.flowProperty;
		Json.put(obj, "targetFlowProperty", exp.handleRef(property));
		Json.put(obj, "targetUnit", Json.asRef(sys.targetUnit));
		Json.put(obj, "targetAmount", sys.targetAmount);

		// map the parameter redefinitions
		GlobalParameters.sync(sys, exp);
		putParameterSets(obj, sys.parameterSets);

		if (exp.db == null)
			return obj;
		mapProcesses(sys, obj);
		mapLinks(sys, obj, exchangeIDs);
		return obj;
	}

	private void mapLinks(
		ProductSystem sys, JsonObject json, TLongLongHashMap exchangeIDs) {
		var array = new JsonArray();
		for (var link : sys.processLinks) {
			var obj = new JsonObject();
			var providerType = providerTypeOf(link);
			Json.put(obj, "provider", exp.handleRef(providerType, link.providerId));
			Json.put(obj, "flow", exp.handleRef(ModelType.FLOW, link.flowId));
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

	private void mapProcesses(ProductSystem sys, JsonObject json) {
		var refs = exp.dbRefs;
		if (refs == null)
			return;
		var array = new JsonArray();
		var types = new ModelType[]{
			ModelType.PROCESS, ModelType.PRODUCT_SYSTEM, ModelType.RESULT};

		for (var id : sys.processes) {
			if (id == null)
				continue;
			long unboxedId = id;
			ModelType type = null;
			for (var t : types) {
				if (refs.descriptorOf(t, unboxedId) != null) {
					type = t;
					break;
				}
			}
			if (type == null)
				continue;
			var ref = exp.handleRef(type, unboxedId);
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
	private TLongLongHashMap exchangeIDs(ProductSystem sys) {
		var map = new TLongLongHashMap();
		if (sys.referenceExchange != null) {
			map.put(sys.referenceExchange.id, -1);
		}
		for (ProcessLink link : sys.processLinks) {
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
}
