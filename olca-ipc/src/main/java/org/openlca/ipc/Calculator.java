package org.openlca.ipc;

import com.google.gson.JsonObject;

class Calculator {

	private final Server server;
	private final RpcRequest req;

	private Calculator(Server server, RpcRequest req) {
		this.server = server;
		this.req = req;
	}

	static RpcResponse doIt(Server server, RpcRequest req) {
		return new Calculator(server, req).run();
	}

	private RpcResponse run() {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("No calculation setup given", req);
		JsonObject setup = req.params.getAsJsonObject();
		return null;
	}
}
