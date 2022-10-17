package org.openlca.core.services;

import static org.openlca.core.services.Util.*;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.JsonPrimitive;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.EnviFlowValue;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.TechFlowValue;
import org.openlca.core.results.UpstreamTree;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.JsonRefs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.util.Strings;

public class JsonResultService {

	private final IDatabase db;
	private final CalculationQueue queue;

	private JsonResultService(IDatabase db, int threadCount) {
		this.db = Objects.requireNonNull(db);
		this.queue = new CalculationQueue(db, Math.max(1, threadCount));
	}

	public static JsonResultService of(IDatabase db) {
		return new JsonResultService(db, 1);
	}

	public static JsonResultService threadCountOf(IDatabase db, int threadCount) {
		return new JsonResultService(db, threadCount);
	}

	public Response<JsonObject> calculate(JsonObject setup) {
		try {
			var r = JsonCalculationSetup.readFrom(setup, DbEntityResolver.of(db));
			if (r.hasError())
				return Response.error(r.error());
			var state = queue.schedule(r.setup());
			return Response.of(JsonUtil.encodeState(state));
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	public Response<JsonObject> getState(String resultId) {
		try {
			var state = queue.get(resultId);
			return Response.of(JsonUtil.encodeState(state));
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	// region: index elements

	public Response<JsonArray> getTechFlows(String resultId) {
		return withResult(resultId, result -> {
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(
					result.techIndex(),
					techFlow -> JsonUtil.encodeTechFlow(techFlow, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getEnviFlows(String resultId) {
		return withResult(resultId, result -> {
			var index = result.enviIndex();
			if (index == null)
				return Response.of(new JsonArray());
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(index,
					enviFlow -> JsonUtil.encodeEnviFlow(enviFlow, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getImpactCategories(String resultId) {
		return withResult(resultId, result -> {
			var index = result.impactIndex();
			if (index == null)
				return Response.of(new JsonArray());
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(index, refs::asRef);
			return Response.of(array);
		});
	}

	// endregion

	// region: technosphere flows

	public Response<JsonArray> getTotalRequirements(String resultId) {
		return withResult(resultId, result -> {
			var tr = result.totalRequirements();
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(tr,
					techValue -> JsonUtil.encodeTechValue(techValue, refs));
			return Response.of(array);
		});
	}

	public Response<JsonObject> getTotalRequirementsOf(
			String resultId, TechFlowId techFlowId) {
		return withResultOfTechFlow(resultId, techFlowId, (result, techFlow) -> {
			var value = result.totalRequirementsOf(techFlow);
			var obj = JsonUtil.encodeTechValue(
					TechFlowValue.of(techFlow, value), JsonRefs.of(db));
			return Response.of(obj);
		});
	}

	// endregion

	// region: flows

	public Response<JsonArray> getTotalFlows(String resultId) {
		return withResult(resultId, result -> {
			if (!result.hasEnviFlows())
				return Response.of(new JsonArray());
			var array = JsonUtil.encodeEnviValues(
					result.getTotalFlows(), JsonRefs.of(db));
			return Response.of(array);
		});
	}

	public Response<JsonObject> getTotalFlowValueOf(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var amount = result.getTotalFlowValueOf(enviFlow);
					var value = EnviFlowValue.of(enviFlow, amount);
					return JsonUtil.encodeEnviValue(value, JsonRefs.of(db));
				}));
	}

	public Response<JsonArray> getTotalFlowValuesOf(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var values = result.getTotalFlowValuesOf(enviFlow);
					return JsonUtil.encodeTechValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonArray> getDirectFlowValuesOf(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var values = result.getDirectFlowValuesOf(enviFlow);
					return JsonUtil.encodeTechValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonArray> getDirectFlowsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					var values = result.getDirectFlowsOf(techFlow);
					return JsonUtil.encodeEnviValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonPrimitive> getDirectFlowOf(
			String resultId, EnviFlowId enviFlowId, TechFlowId techFlowId) {
		return withResult(resultId, result -> join(
				enviFlowOf(result, enviFlowId),
				techFlowOf(result, techFlowId),
				(enviFlow, techFlow) -> {
					double amount = result.getDirectFlowOf(enviFlow, techFlow);
					return Response.of(new JsonPrimitive(amount));
				}));
	}

	public Response<JsonArray> getTotalFlowsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					var values = result.getTotalFlowsOf(techFlow);
					return JsonUtil.encodeEnviValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonPrimitive> getTotalFlowOf(
			String resultId, EnviFlowId enviFlowId, TechFlowId techFlowId) {
		return withResult(resultId, result -> join(
				enviFlowOf(result, enviFlowId),
				techFlowOf(result, techFlowId),
				(enviFlow, techFlow) -> {
					var value = result.getTotalFlowOf(enviFlow, techFlow);
					return Response.of(new JsonPrimitive(value));
				}));
	}


	// endregion

	// region: impacts

	public Response<JsonArray> getTotalImpacts(String resultId) {
		return withResult(resultId, result -> {
			if (!result.hasImpacts())
				return Response.of(new JsonArray());
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(
					result.totalImpacts(),
					value -> JsonUtil.encodeImpact(value, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getImpactOfEnviFlows(
			String resultId, String impactId) {
		return withResult(resultId, result -> impactOf(result, impactId)
				.map(impact -> JsonUtil.encodeArray(
						result.impactOfEnviFlows(impact),
						value -> JsonUtil.encodeEnviValue(value, JsonRefs.of(db)))));
	}

	public Response<JsonArray> getImpactOfTechFlows(
			String resultId, String impactId) {
		return withResult(resultId, result -> impactOf(result, impactId)
				.map(impact -> JsonUtil.encodeArray(
						result.impactOfTechFlows(impact),
						value -> JsonUtil.encodeTechValue(value, JsonRefs.of(db)))));
	}

	// endregion

	// region: costs

	public Response<JsonArray> getDirectCosts(String resultId) {
		return withResult(resultId, result -> {
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(
					result.directCosts(),
					value -> JsonUtil.encodeTechValue(value, refs));
			return Response.of(array);
		});
	}

	public Response<JsonPrimitive> getTotalCosts(String resultId) {
		return withResult(resultId, result -> {
			double costs = result.totalCosts();
			return Response.of(new JsonPrimitive(costs));
		});
	}

	public Response<JsonArray> getTotalCostsByTechFlow(String resultId) {
		return withResult(resultId, result -> {
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(
					result.totalCostsByTechFlow(),
					value -> JsonUtil.encodeTechValue(value, refs));
			return Response.of(array);
		});
	}

	// endregion

	// region: upstream trees

	public Response<JsonArray> getUpstreamOfEnviFlow(
			String resultId, String path, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var tree = UpstreamTree.of(result.provider(), enviFlow);
					return getUpstreamNodes(path, tree);
				}));
	}

	public Response<JsonArray> getUpstreamNodes(
			String resultId, String path, ImpactDescriptor impact) {
		return withResult(resultId, result -> {
			var tree = UpstreamTree.of(result.provider(), impact);
			return getUpstreamNodes(path, tree);
		});
	}

	public Response<JsonArray> getUpstreamNodesForCosts(
			String resultId, String path) {
		return withResult(resultId, result -> {
			var tree = UpstreamTree.costsOf(result.provider());
			return getUpstreamNodes(path, tree);
		});
	}

	private JsonArray getUpstreamNodes(String path, UpstreamTree tree) {
		var nodes = UpstreamPath.parse(path).selectChilds(tree);
		var refs = JsonRefs.of(db);
		return JsonUtil.encodeArray(
				nodes, node -> JsonUtil.encodeUpstreamNode(node, refs));
	}

	// endregion

	public Response<JsonObject> dispose(String resultId) {
		try {
			queue.dispose(resultId);
			var obj = new JsonObject();
			Json.put(obj, "@id", resultId);
			return Response.of(obj);
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	private <T> Response<T> withResult(
			String resultId, Function<LcaResult, Response<T>> fn) {
		var res = resultOf(queue, resultId);
		if (res.isEmpty())
			return Response.empty();
		return res.isError()
				? Response.error(res.error())
				: fn.apply(res.value());
	}

	private <T> Response<T> withResultOfTechFlow(
			String resultId, TechFlowId techFlowId,
			BiFunction<LcaResult, TechFlow, Response<T>> fn) {
		return withResult(resultId, result -> {
			if (techFlowId == null)
				return Response.error("no tech-flow ID provided");
			var techFlow = techFlowId.findTechFlowOf(result).orElse(null);
			return techFlow == null
					? Response.error("invalid tech-flow ID: " + techFlowId)
					: fn.apply(result, techFlow);
		});
	}

	private Response<ImpactDescriptor> impactOf(
			LcaResult result, String impactId) {
		if (!result.hasImpacts())
			return Response.error("not an LCIA result");
		if (Strings.nullOrEmpty(impactId))
			return Response.error("no ID of LCIA category given");
		ImpactDescriptor impact = null;
		for (var i : result.impactIndex()) {
			if (Objects.equals(i.refId, impactId)) {
				impact = i;
				break;
			}
		}
		return impact != null
				? Response.of(impact)
				: Response.error("no LCIA category exists for ID=" + impactId);
	}

}
