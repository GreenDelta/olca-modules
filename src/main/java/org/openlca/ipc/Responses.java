package org.openlca.ipc;

class Responses {

	static RpcResponse serverError(Exception e) {
		RpcError error = new RpcError();
		error.code = -32000;
		error.message = "An internal server error occurred: " + e.getMessage();
		RpcResponse response = new RpcResponse();
		response.error = error;
		return response;
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
		String method = req.method == null ? "?" : req.method;
		error.message = "Does not understand: " + method;
		RpcResponse response = new RpcResponse();
		response.error = error;
		response.id = req.id;
		return response;
	}
}
