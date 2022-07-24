package org.openlca.ipc.handlers;

import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.DbRefs;

import java.util.function.Function;

class ResultRequest {

	private final RpcRequest req;
	private final JsonObject reqParam;
	private final FullResult result;
	private final DbRefs refs;

	private TechFlow _techFlow;
	private EnviFlow _enviFlow;
	private ImpactDescriptor _impact;

	private ResultRequest(
			RpcRequest req,
			JsonObject reqParam,
			FullResult result,
			DbRefs refs) {
		this.req = req;
		this.reqParam = reqParam;
		this.result = result;
		this.refs = refs;
	}

	static RpcResponse of(RpcRequest req, HandlerContext context,
			Function<ResultRequest, RpcResponse> handler) {
		var paramQ = parameterOf(req);
		if (paramQ.isError())
			return paramQ.error();
		var param = paramQ.value();
		var cached = resultOf(context, req, param);
		if (cached.isError())
			return cached.error();
		var cachedResult = cached.value();
		if (!(cachedResult.result() instanceof FullResult result))
			return Responses.badRequest(
					"method cannot be called on this type of result", req);

		var reqData = new ResultRequest(
				req,
				param,
				result,
				cachedResult.refs());
		return handler.apply(reqData);
	}

	private static Effect<JsonObject> parameterOf(RpcRequest req) {
		return req == null || req.params == null || !req.params.isJsonObject()
				? Effect.error(Responses.invalidParams(req))
				: Effect.ok(req.params.getAsJsonObject());
	}

	private static Effect<CachedResult<?>> resultOf(
			HandlerContext context, RpcRequest req, JsonObject param) {
		var resultId = Json.getString(param, "resultId");
		if (resultId == null)
			return Effect.error(Responses.invalidParams("resultId is missing", req));
		CachedResult<?> result = context.getCached(CachedResult.class, resultId);
		return result != null
				? Effect.ok(result)
				: Effect.error(Responses.notFound(
				"no such result exists; id=" + resultId, req));
	}

	RpcRequest request() {
		return req;
	}

	DbRefs refs() {
		return refs;
	}

	JsonObject requestParameter() {
		return reqParam;
	}

	FullResult result() {
		return result;
	}

	EnviFlow enviFlow() {
		if (_enviFlow != null)
			return _enviFlow;
		var idx = result.enviIndex();
		if (idx == null)
			return null;
		var flowObj = Json.getObject(reqParam, "flow");
		if (flowObj == null)
			return null;
		var flowId = Json.getRefId(flowObj, "flow");
		if (flowId == null)
			return null;
		var locId = Json.getRefId(flowObj, "location");
		for (var enviFlow : idx) {
			if (!flowId.equals(enviFlow.flow().refId))
				continue;
			if (locId == null && enviFlow.location() == null)
				return _enviFlow = enviFlow;
			if (locId == null || enviFlow.location() == null)
				continue;
			if (locId.equals(enviFlow.location().refId))
				return _enviFlow = enviFlow;
		}
		return null;
	}

	TechFlow techFlow() {
		if (_techFlow != null)
			return _techFlow;
		var idx = result.techIndex();
		if (idx == null)
			return null;
		var providerObj = Json.getObject(reqParam, "provider");
		if (providerObj == null)
			return null;
		var providerId = Json.getRefId(providerObj, "provider");
		var flowId = Json.getRefId(providerObj, "flow");
		if (providerId == null || flowId == null)
			return null;
		for (var techFlow : idx) {
			var provider = techFlow.provider();
			var flow = techFlow.flow();
			if (provider == null || flow == null)
				continue;
			if (providerId.equals(provider.refId) && flowId.equals(flow.refId)) {
				_techFlow = techFlow;
				return techFlow;
			}
		}
		return null;
	}

	ImpactDescriptor impact() {
		if (_impact != null)
			return _impact;
		var idx = result.impactIndex();
		if (idx == null)
			return null;
		var impactId = Json.getRefId(reqParam, "impactCategory");
		if (impactId == null)
			return null;
		for (var impact : idx) {
			if (impactId.equals(impact.refId)) {
				_impact = impact;
				return impact;
			}
		}
		return null;
	}


	RpcResponse providerMissing() {
		return Responses.invalidParams(
				"Missing or invalid provider parameter", req);
	}

	RpcResponse impactMissing() {
		return Responses.invalidParams(
				"Missing or invalid impact category parameter", req);
	}

	RpcResponse enviFlowMissing() {
		return Responses.invalidParams(
				"Missing or invalid flow parameter", req);
	}

	RpcResponse noCostResults() {
		return Responses.badRequest(
				"The result has no cost results", req);
	}

}
