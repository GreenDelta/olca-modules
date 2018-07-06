package org.openlca.ipc.handlers;

import com.google.gson.JsonObject;

import org.openlca.core.database.IDatabase;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

public class ExportHandler {

	private final HandlerContext context;

	public ExportHandler(HandlerContext context) {
		this.context = context;
	}

	@Rpc("export/excel")
	public RpcResponse excel(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.badRequest("No @id given", req);
		JsonObject obj = req.params.getAsJsonObject();
		String id = Json.getString(obj, "@id");
		if (id == null)
			return Responses.badRequest("No @id given", req);
		Object val = context.cache.get(id);
		if (val == null)
			return Responses.notFound("No object found with @id=" + id, req);
		// TODO: implement Excel export
		return Responses.notImplemented("Not yet implemented", req);
	}

}
