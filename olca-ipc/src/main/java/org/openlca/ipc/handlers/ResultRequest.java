package org.openlca.ipc.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.services.Response;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import java.util.function.Function;

class ResultRequest {

	private final RpcRequest req;
	private final JsonObject reqParam;
	private final String resultId;

	private TechFlow _techFlow;
	private EnviFlow _enviFlow;
	private ImpactDescriptor _impact;

	private ResultRequest(RpcRequest req, JsonObject reqParam, String resultId) {
		this.req = req;
		this.reqParam = reqParam;
		this.resultId = resultId;
	}

	static RpcResponse of(RpcRequest req,
			Function<ResultRequest, Response<? extends JsonElement>> handler) {
		Effect<JsonObject> paramQ =
				req == null || req.params == null || !req.params.isJsonObject()
						? Effect.error(Responses.invalidParams(req))
						: Effect.ok(req.params.getAsJsonObject());
		if (paramQ.isError())
			return paramQ.error();
		var param = paramQ.value();
		var resultId = Json.getString(param, "@id");
		if (Strings.nullOrEmpty(resultId))
			return Responses.invalidParams("no result ID @id provided", req);
		var reqData = new ResultRequest(req, param, resultId);
		return Responses.of(handler.apply(reqData), req);
	}

	RpcRequest request() {
		return req;
	}

	JsonObject requestParameter() {
		return reqParam;
	}

	String id() {
		return resultId;
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

	String impactId() {
		return Json.getRefId(reqParam, "impactCategory");
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
