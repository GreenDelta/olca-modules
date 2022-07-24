package org.openlca.ipc.handlers;

import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.results.FullResult;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.output.DbRefs;

import java.util.function.Function;

record ResultRequest(
		RpcRequest req,
		FullResult result,
		TechFlow techFlow,
		EnviFlow enviFlow,
		ImpactDescriptor impact,
		LocationDescriptor location,
		DbRefs refs) {

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
				result,
				providerOf(result.techIndex(), param),
				enviFlowOf(result.enviIndex(), param),
				impactOf(result.impactIndex(), param),
				null, // TODO: read location
				cachedResult.refs());
		return handler.apply(reqData);
	}

	RpcResponse impactMissing() {
		return Responses.invalidParams(
				"Missing or invalid impact category parameter", req);
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

	private static EnviFlow enviFlowOf(EnviIndex idx, JsonObject param) {
		if (idx == null)
			return null;
		var flowObj = Json.getObject(param, "flow");
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
				return enviFlow;
			if (locId == null || enviFlow.location() == null)
				continue;
			if (locId.equals(enviFlow.location().refId))
				return enviFlow;
		}
		return null;
	}

	private static TechFlow providerOf(TechIndex idx, JsonObject param) {
		if (idx == null)
			return null;
		var providerObj = Json.getObject(param, "provider");
		if (providerObj == null)
			return null;
		var providerId = Json.getRefId(param, "provider");
		var flowId = Json.getRefId(param, "flow");
		if (providerId == null || flowId == null)
			return null;
		for (var techFlow : idx) {
			var provider = techFlow.provider();
			var flow = techFlow.flow();
			if (provider == null || flow == null)
				continue;
			if (providerId.equals(provider.refId) && flowId.equals(flow.refId))
				return techFlow;
		}
		return null;
	}

	private static ImpactDescriptor impactOf(ImpactIndex idx,  JsonObject param) {
		if (idx == null)
			return null;
		var impactId = Json.getRefId(param, "impactCategory");
		if (impactId == null)
			return null;
		for (var impact : idx) {
			if (impactId.equals(impact.refId))
				return impact;
		}
		return null;
	}

}
