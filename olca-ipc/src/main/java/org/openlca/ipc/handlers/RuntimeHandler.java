package org.openlca.ipc.handlers;

import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeHandler {

	private final HandlerContext context;

	public RuntimeHandler(HandlerContext context) {
		this.context = context;
	}

	@Rpc("runtime/shutdown")
	public RpcResponse shutdown(RpcRequest req) {
		Logger log = LoggerFactory.getLogger(getClass());
		log.info("Shutdown server and close database");
		try {
			context.server.stop();
			context.db.close();
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}
}
