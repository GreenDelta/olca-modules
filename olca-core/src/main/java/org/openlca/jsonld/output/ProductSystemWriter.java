package org.openlca.jsonld.output;

import java.util.List;
import java.util.Objects;

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

public class ProductSystemWriter implements JsonWriter<ProductSystem> {

	private final JsonExport exp;
	private final NodeResolver nodes;

	public ProductSystemWriter(JsonExport exp) {
		this.exp = Objects.requireNonNull(exp);
		this.nodes = NodeResolver.of(exp);
	}

	@Override
	public JsonObject write(ProductSystem sys) {
		var obj = Util.init(exp, sys);
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
		if (sys.targetFlowPropertyFactor != null) {
			property = sys.targetFlowPropertyFactor.flowProperty;
		}
		Json.put(obj, "targetFlowProperty", exp.handleRef(property));
		Json.put(obj, "targetUnit", Json.asRef(sys.targetUnit));
		Json.put(obj, "targetAmount", sys.targetAmount);

		// map the parameter redefinitions
		GlobalParameters.sync(sys, exp);
		mapParameterSets(obj, sys.parameterSets);

		mapProcesses(sys, obj);
		mapAnalysisGroups(sys, obj);
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
		var array = new JsonArray();
		for (var id : sys.processes) {
			var ref = nodes.refOf(id);
			if (ref != null) {
				array.add(ref);
			}
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

	private void mapParameterSets(JsonObject obj, List<ParameterRedefSet> sets) {
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
				var params = ParameterWriter.mapRedefs(exp, set.parameters);
				Json.put(jsonSet, "parameters", params);
			}
		}
		Json.put(obj, "parameterSets", jsonSets);
	}

	private void mapAnalysisGroups(ProductSystem sys, JsonObject root) {
		if (sys.analysisGroups.isEmpty())
			return;
		var array = new JsonArray(sys.analysisGroups.size());
		for (var group : sys.analysisGroups) {
			var obj = new JsonObject();
			Json.put(obj, "name", group.name);
			Json.put(obj, "color", group.color);
			if (!group.processes.isEmpty()) {
				var processes = new JsonArray(group.processes.size());
				for (var pid : group.processes) {
					if (!sys.processes.contains(pid))
						continue;
					var ref = nodes.refOf(pid);
					if (ref != null) {
						processes.add(ref);
					}
				}
				Json.put(obj, "processes", processes);
			}
			array.add(obj);
		}
		Json.put(root, "analysisGroups", array);
	}

	/// The nodes in a product system can be processes, sub-systems, or
	/// results, but sometimes we only have the ID of that node. We then
	/// need to find the object reference for that ID testing the possible
	/// types.
	private record NodeResolver(
			JsonExport exp, JsonRefs refs, ModelType[] types
	) {

		static NodeResolver of(JsonExport exp) {
			return new NodeResolver(exp, exp.dbRefs, new ModelType[]{
					ModelType.PROCESS,
					ModelType.PRODUCT_SYSTEM,
					ModelType.RESULT
			});
		}

		JsonObject refOf(Long id) {
			if (id == null || refs == null)
				return null;
			long unboxedId = id;
			ModelType type = null;
			for (var t : types) {
				if (refs.descriptorOf(t, unboxedId) != null) {
					type = t;
					break;
				}
			}
			return type != null
					? exp.handleRef(type, unboxedId)
					: null;
		}
	}
}
