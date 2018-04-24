package org.openlca.ipc;

import com.google.gson.JsonPrimitive;

class Responses {

	static RpcResponse ok(RpcRequest req) {
		RpcResponse response = new RpcResponse();
		if (req != null) {
			response.id = req.id;
		}
		response.result = new JsonPrimitive("ok");
		return response;
	}

	static RpcResponse serverError(Exception e, RpcRequest req) {
		RpcError error = new RpcError();
		error.code = -32000;
		error.message = "Unhandled server error: " + e.getMessage();
		return response(error, req);
	}

	static RpcResponse requestError(String message) {
		RpcError error = new RpcError();
		error.code = -32600;
		error.message = "The JSON sent is not a valid Request object: "
				+ message;
		RpcResponse response = new RpcResponse();
		response.error = error;
		return response;
	}

	static RpcResponse unknownMethod(RpcRequest req) {
		RpcError error = new RpcError();
		error.code = -32601;
		String method = req == null ? "?" : req.method;
		error.message = "Does not understand: " + method;
		return response(error, req);
	}

	static RpcResponse invalidParams(String message, RpcRequest req) {
		RpcError error = new RpcError();
		error.code = -32602;
		error.message = "Invalid params: " + message;
		return response(error, req);
	}

	private static RpcResponse response(RpcError error, RpcRequest req) {
		RpcResponse response = new RpcResponse();
		response.error = error;
		if (req != null) {
			response.id = req.id;
		}
		return response;
	}

}
