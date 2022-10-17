package org.openlca.ipc.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.services.EnviFlowId;
import org.openlca.core.services.Response;
import org.openlca.core.services.TechFlowId;
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

	private TechFlowId _techFlow;
	private EnviFlowId _enviFlow;

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

	EnviFlowId enviFlow() {
		if (_enviFlow != null)
			return _enviFlow;
		var obj = Json.getObject(reqParam, "enviFlow");
		_enviFlow = EnviFlowId.of(obj).orElse(null);
		return _enviFlow;
	}

	TechFlowId techFlow() {
		if (_techFlow != null)
			return _techFlow;
		var obj = Json.getObject(reqParam, "techFlow");
		_techFlow = TechFlowId.of(obj).orElse(null);
		return _techFlow;
	}

	String impact() {
		return Json.getRefId(reqParam, "impactCategory");
	}
}
