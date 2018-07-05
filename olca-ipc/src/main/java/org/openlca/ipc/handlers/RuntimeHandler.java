package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeHandler {

	private final IDatabase db;
	private final Server server;

	public  RuntimeHandler(IDatabase db, Server server) {
		this.db = db;
		this.server = server;
	}

	@Rpc("runtime/shutdown")
	public RpcResponse shutdown(RpcRequest req) {
		Logger log = LoggerFactory.getLogger(getClass());
		log.info("Shutdown server and close database");
		try {
			server.stop();
			db.close();
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}
}
