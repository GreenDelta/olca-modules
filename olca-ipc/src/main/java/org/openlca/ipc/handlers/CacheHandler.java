package org.openlca.ipc.handlers;

import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.Server;
import org.openlca.jsonld.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class CacheHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Server server;

	public CacheHandler(Server server) {
		this.server = server;
	}

	@Rpc("dispose")
	public RpcResponse get(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.error(400, "No object with '@id' provided", req);
		JsonObject param = req.params.getAsJsonObject();
		String id = Json.getString(param, "@id");
		if (id == null)
			return Responses.error(400, "No '@id' provided", req);
		boolean removed = server.memory.remove(id) != null;
		if (removed) {
			log.info("Removed {} from memory", id);
			return Responses.ok(req);
		}
		return Responses.ok("Did not find something with @id="
				+ id + "in memory; did nothing", req);
	}
}
