package org.openlca.core.services;

import java.util.Objects;
import java.util.function.Function;

import com.google.gson.JsonPrimitive;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.DbEntityResolver;
import org.openlca.core.results.LcaResult;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.JsonRefs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

	public Response<JsonArray> getTotalRequirements(String resultId) {
		return withResult(resultId, result -> {
			var tr = result.totalRequirements();
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(tr,
					techValue -> JsonUtil.encodeTechFlowValue(techValue, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getTotalImpacts(String resultId) {
		return withResult(resultId, result -> {
			if (!result.hasImpacts())
				return Response.of(new JsonArray());
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(
					result.totalImpacts(),
					value -> JsonUtil.encodeImpactValue(value, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getTotalFlows(String resultId) {
		return withResult(resultId, result -> {
			if (!result.hasEnviFlows())
				return Response.of(new JsonArray());
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(
					result.totalFlows(),
					value -> JsonUtil.encodeEnviFlowValue(value, refs));
			return Response.of(array);
		});
	}

	public Response<JsonArray> getDirectCosts(String resultId) {
		return withResult(resultId, result -> {
			var refs = JsonRefs.of(db);
			var array = JsonUtil.encodeArray(
					result.directCosts(),
					value -> JsonUtil.encodeTechFlowValue(value, refs));
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
					value -> JsonUtil.encodeTechFlowValue(value, refs));
			return Response.of(array);
		});
	}

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
		try {
			var state = queue.get(resultId);
			if (state.isError())
				return Response.error(state.error());
			if (state.isEmpty())
				return Response.empty();
			if (state.isScheduled() || !state.isReady())
				return Response.error("result not yet ready");
			return fn.apply(state.result());
		} catch (Exception e) {
			return Response.error(e);
		}
	}
}
