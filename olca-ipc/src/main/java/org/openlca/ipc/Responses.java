package org.openlca.ipc;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Utility methods for creating RPC responses.
 */
public class Responses {

	private Responses() {
	}

	public static RpcResponse ok(RpcRequest req) {
		RpcResponse response = new RpcResponse();
		if (req != null) {
			response.id = req.id;
		}
		response.result = new JsonPrimitive("ok");
		return response;
	}

	public static RpcResponse ok(String message, RpcRequest req) {
		String m = message;
		if (m == null)
			m = "";
		return ok(new JsonPrimitive(m), req);
	}

	public static RpcResponse ok(JsonElement result, RpcRequest req) {
		RpcResponse response = new RpcResponse();
		if (req != null) {
			response.id = req.id;
		}
		response.result = result;
		return response;
	}

	public static RpcResponse badRequest(String message, RpcRequest req) {
		return error(400, message, req);
	}

	public static RpcResponse notFound(String message, RpcRequest req) {
		return error(404, message, req);
	}

	public static RpcResponse internalServerError(String message,
			RpcRequest req) {
		return error(500, message, req);
	}

	public static RpcResponse notImplemented(String message, RpcRequest req) {
		return error(501, message, req);
	}

	public static RpcResponse error(int code, String message, RpcRequest req) {
		RpcError error = new RpcError();
		error.code = code;
		error.message = message;
		return response(error, req);
	}

	public static RpcResponse serverError(Exception e, RpcRequest req) {
		RpcError error = new RpcError();
		error.code = -32000;
		error.message = "Unhandled server error: " + e.getMessage();
		return response(error, req);
	}

	public static RpcResponse requestError(String message) {
		RpcError error = new RpcError();
		error.code = -32600;
		error.message = "The JSON sent is not a valid Request object: "
				+ message;
		RpcResponse response = new RpcResponse();
		response.error = error;
		return response;
	}

	public static RpcResponse unknownMethod(RpcRequest req) {
		RpcError error = new RpcError();
		error.code = -32601;
		String method = req == null ? "?" : req.method;
		error.message = "Does not understand: " + method;
		return response(error, req);
	}

	public static RpcResponse invalidParams(String message, RpcRequest req) {
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
