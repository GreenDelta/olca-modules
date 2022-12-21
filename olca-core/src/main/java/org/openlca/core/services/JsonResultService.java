package org.openlca.core.services;

import static org.openlca.core.services.JsonUtil.*;
import static org.openlca.core.services.Util.*;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.JsonPrimitive;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.matrix.NwSetTable;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
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
			return Response.of(encodeState(state));
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	public Response<JsonObject> getState(String resultId) {
		try {
			var state = queue.get(resultId);
			return Response.of(encodeState(state));
		} catch (Exception e) {
			return Response.error(e);
		}
	}

	// region: index elements

	public Response<JsonArray> getTechFlows(String resultId) {
		return withResult(resultId, result -> {
			var refs = JsonRefs.of(db);
			var array = encodeArray(
					result.techIndex(),
					techFlow -> encodeTechFlow(techFlow, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getEnviFlows(String resultId) {
		return withResult(resultId, result -> {
			var index = result.enviIndex();
			if (index == null)
				return Response.of(new JsonArray());
			var refs = JsonRefs.of(db);
			var array = encodeArray(index,
					enviFlow -> encodeEnviFlow(enviFlow, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getImpactCategories(String resultId) {
		return withResult(resultId, result -> {
			var index = result.impactIndex();
			if (index == null)
				return Response.of(new JsonArray());
			var refs = JsonRefs.of(db);
			var array = encodeArray(index, refs::asRef);
			return Response.of(array);
		});
	}

	// endregion

	// region: technosphere flows

	public Response<JsonArray> getScalingFactors(String resultId) {
		return withResult(resultId, result -> {
			var s = result.getScalingFactors();
			var refs = JsonRefs.of(db);
			var array = encodeArray(s,
					techValue -> encodeTechValue(techValue, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getTotalRequirements(String resultId) {
		return withResult(resultId, result -> {
			var tr = result.totalRequirements();
			var refs = JsonRefs.of(db);
			var array = encodeArray(tr,
					techValue -> encodeTechValue(techValue, refs));
			return Response.of(array);
		});
	}

	public Response<JsonObject> getTotalRequirementsOf(
			String resultId, TechFlowId techFlowId) {
		return withResultOfTechFlow(resultId, techFlowId, (result, techFlow) -> {
			var value = result.totalRequirementsOf(techFlow);
			var obj = encodeTechValue(
					TechFlowValue.of(techFlow, value), JsonRefs.of(db));
			return Response.of(obj);
		});
	}

	public Response<JsonArray> getDirectRequirementsOf(
			String resultId, TechFlowId techFlowId) {
		return withResultOfTechFlow(resultId, techFlowId, (result, techFlow) -> {
			var values = result.directRequirementsOf(techFlow);
			var refs = JsonRefs.of(db);
			var array = encodeArray(values,
					techValue -> encodeTechValue(techValue, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getUnscaledRequirementsOf(
			String resultId, TechFlowId techFlowId) {
		return withResultOfTechFlow(resultId, techFlowId, (result, techFlow) -> {
			var values = result.unscaledRequirementsOf(techFlow);
			var refs = JsonRefs.of(db);
			var array = encodeArray(values,
					techValue -> encodeTechValue(techValue, refs));
			return Response.of(array);
		});
	}

	// endregion

	// region: flows

	public Response<JsonArray> getTotalFlows(String resultId) {
		return withResult(resultId, result -> {
			if (!result.hasEnviFlows())
				return Response.of(new JsonArray());
			var array = encodeEnviValues(
					result.getTotalFlows(), JsonRefs.of(db));
			return Response.of(array);
		});
	}

	public Response<JsonPrimitive> getTotalFlowValueOf(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var value = result.getTotalFlowValueOf(enviFlow);
					return new JsonPrimitive(value);
				}));
	}

	public Response<JsonArray> getDirectFlowValuesOf(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var values = result.getDirectFlowValuesOf(enviFlow);
					return encodeTechValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonArray> getTotalFlowValuesOf(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var values = result.getTotalFlowValuesOf(enviFlow);
					return encodeTechValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonArray> getDirectFlowsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					var values = result.getDirectFlowsOf(techFlow);
					return encodeEnviValues(values, JsonRefs.of(db));
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

	public Response<JsonArray> getTotalFlowsOfOne(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					var values = result.getTotalFlowsOfOne(techFlow);
					return encodeEnviValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonPrimitive> getTotalFlowOfOne(
			String resultId, EnviFlowId enviFlowId, TechFlowId techFlowId) {
		return withResult(resultId, result -> join(
				enviFlowOf(result, enviFlowId),
				techFlowOf(result, techFlowId),
				(enviFlow, techFlow) -> {
					var value = result.getTotalFlowOf(enviFlow, techFlow);
					return Response.of(new JsonPrimitive(value));
				}));
	}

	public Response<JsonArray> getTotalFlowsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					var values = result.getTotalFlowsOf(techFlow);
					return encodeEnviValues(values, JsonRefs.of(db));
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
			var array = encodeImpactValues(
					result.getTotalImpacts(), JsonRefs.of(db));
			return Response.of(array);
		});
	}

	public Response<JsonPrimitive> getTotalImpactValueOf(
			String resultId, String impactId) {
		return withResult(resultId, result -> impactOf(result, impactId)
				.map(impact -> {
					double value = result.getTotalImpactValueOf(impact);
					return new JsonPrimitive(value);
				})
		);
	}

	public Response<JsonArray> getNormalizedImpacts(String resultId) {
		return withResult(resultId, result -> {
			var impacts = result.getTotalImpacts();
			if (impacts.isEmpty())
				return Response.empty();
			var state = queue.get(resultId);
			if (!state.isReady())
				return Response.error("no result ready");
			var setup = state.setup();
			if (setup.nwSet() == null)
				return Response.error("no nw-set was defined");
			var factors = NwSetTable.of(db, setup.nwSet());
			var normalized = factors.normalize(impacts);
			return Response.of(encodeImpactValues(normalized, JsonRefs.of(db)));
		});
	}

	public Response<JsonArray> getWeightedImpacts(String resultId) {
		return withResult(resultId, result -> {
			var impacts = result.getTotalImpacts();
			if (impacts.isEmpty())
				return Response.empty();
			var state = queue.get(resultId);
			if (!state.isReady())
				return Response.error("no result ready");
			var setup = state.setup();
			if (setup.nwSet() == null)
				return Response.error("no nw-set was defined");
			var factors = NwSetTable.of(db, setup.nwSet());
			var weighted = factors.weight(impacts);
			return Response.of(encodeImpactValues(weighted, JsonRefs.of(db)));
		});
	}

	public Response<JsonArray> getDirectImpactValuesOf(
			String resultId, String impactId) {
		return withResult(resultId, result -> impactOf(result, impactId)
				.map(impact -> {
					var values = result.getDirectImpactValuesOf(impact);
					return encodeTechValues(values, JsonRefs.of(db));
				})
		);
	}

	public Response<JsonArray> getTotalImpactValuesOf(
			String resultId, String impactId) {
		return withResult(resultId, result -> impactOf(result, impactId)
				.map(impact -> encodeTechValues(
						result.getTotalImpactValuesOf(impact), JsonRefs.of(db))));
	}

	public Response<JsonArray> getDirectImpactsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> encodeImpactValues(
						result.getDirectImpactsOf(techFlow), JsonRefs.of(db))));
	}

	public Response<JsonPrimitive> getDirectImpactOf(
			String resultId, String impactId, TechFlowId techFlowId) {
		return withResult(resultId, result -> join(
				impactOf(result, impactId),
				techFlowOf(result, techFlowId),
				(impact, techFlow) -> {
					double value = result.getDirectImpactOf(impact, techFlow);
					return Response.of(new JsonPrimitive(value));
				}));
	}

	public Response<JsonArray> getTotalImpactsOfOne(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					var values = result.getTotalImpactsOfOne(techFlow);
					return encodeImpactValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonPrimitive> getTotalImpactOfOne(
			String resultId, String impactId, TechFlowId techFlowId) {
		return withResult(resultId, result -> join(
				impactOf(result, impactId),
				techFlowOf(result, techFlowId),
				(impact, techFlow) -> {
					double value = result.getTotalImpactOfOne(impact, techFlow);
					return Response.of(new JsonPrimitive(value));
				}));
	}

	public Response<JsonArray> getTotalImpactsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					var values = result.getTotalImpactsOf(techFlow);
					return encodeImpactValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonPrimitive> getTotalImpactOf(
			String resultId, String impactId, TechFlowId techFlowId) {
		return withResult(resultId, result -> join(
				impactOf(result, impactId),
				techFlowOf(result, techFlowId),
				(impact, techFlow) -> {
					double value = result.getTotalImpactOf(impact, techFlow);
					return Response.of(new JsonPrimitive(value));
				}));
	}

	public Response<JsonArray> getImpactFactorsOf(
			String resultId, String impactId) {
		return withResult(resultId, result -> impactOf(result, impactId)
				.map(impact -> {
					var values = result.getImpactFactorsOf(impact);
					return encodeEnviValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonPrimitive> getImpactFactorOf(
			String resultId, String impactId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> join(
				impactOf(result, impactId),
				enviFlowOf(result, enviFlowId),
				(impact, enviFlow) -> {
					double value = result.getImpactFactorOf(impact, enviFlow);
					return Response.of(new JsonPrimitive(value));
				}));
	}

	public Response<JsonArray> getFlowImpactsOfOne(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var values = result.getFlowImpactsOfOne(enviFlow);
					return encodeImpactValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonArray> getFlowImpactsOf(
			String resultId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> enviFlowOf(result, enviFlowId)
				.map(enviFlow -> {
					var values = result.getFlowImpactsOf(enviFlow);
					return encodeImpactValues(values, JsonRefs.of(db));
				}));
	}

	public Response<JsonPrimitive> getFlowImpactOf(
			String resultId, String impactId, EnviFlowId enviFlowId) {
		return withResult(resultId, result -> join(
				impactOf(result, impactId),
				enviFlowOf(result, enviFlowId),
				(impact, enviFlow) -> {
					double value = result.getFlowImpactOf(impact, enviFlow);
					return Response.of(new JsonPrimitive(value));
				}));
	}

	public Response<JsonArray> getFlowImpactValuesOf(
			String resultId, String impactId) {
		return withResult(resultId, result -> impactOf(result, impactId)
				.map(impact -> {
					var values = result.getFlowImpactValuesOf(impact);
					return encodeEnviValues(values, JsonRefs.of(db));
				}));
	}

	// endregion

	// region: costs

	public Response<JsonPrimitive> getTotalCosts(String resultId) {
		return withResult(resultId, result -> {
			double value = result.getTotalCosts();
			return Response.of(new JsonPrimitive(value));
		});
	}

	public Response<JsonArray> getDirectCostValues(String resultId) {
		return withResult(resultId, result -> {
			var values = result.getDirectCostValues();
			var array = encodeTechValues(values, JsonRefs.of(db));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getTotalCostValues(String resultId) {
		return withResult(resultId, result -> {
			var values = result.getTotalCostValues();
			var array = encodeTechValues(values, JsonRefs.of(db));
			return Response.of(array);
		});
	}

	public Response<JsonPrimitive> getDirectCostsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					double value = result.getDirectCostsOf(techFlow);
					return new JsonPrimitive(value);
				}));
	}

	public Response<JsonPrimitive> getTotalCostsOfOne(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					double value = result.getTotalCostsOfOne(techFlow);
					return new JsonPrimitive(value);
				}));
	}

	public Response<JsonPrimitive> getTotalCostsOf(
			String resultId, TechFlowId techFlowId) {
		return withResult(resultId, result -> techFlowOf(result, techFlowId)
				.map(techFlow -> {
					double value = result.getTotalCostsOf(techFlow);
					return new JsonPrimitive(value);
				}));
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

	public Response<JsonArray> getUpstreamOfImpactCategory(
			String resultId, String path, String impactId) {
		return withResult(resultId, result -> impactCategoryOf(result, impactId)
				.map(impact -> {
					var tree = UpstreamTree.of(result.provider(), impact);
					return getUpstreamNodes(path, tree);
				}));
	}

	public Response<JsonArray> getUpstreamOfCosts(String resultId, String path) {
		return withResult(resultId, result -> {
			var tree = UpstreamTree.costsOf(result.provider());
			var nodes = getUpstreamNodes(path, tree);
			return Response.of(nodes);
		});
	}

	private JsonArray getUpstreamNodes(String path, UpstreamTree tree) {
		var nodes = UpstreamPath.parse(path).selectChilds(tree);
		var refs = JsonRefs.of(db);
		return encodeArray(nodes, node -> encodeUpstreamNode(node, refs));
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
