package org.openlca.ipc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.jsonld.output.JsonExport;

import java.util.HashMap;
import java.util.Map;

public class Server extends NanoHTTPD {

	private final IDatabase db;

	public Server(int port, IDatabase db) {
		super(port);
		this.db = db;
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Response serve(IHTTPSession session) {
		String method = session.getMethod().name();
		if (!"POST".equals(method))
			return serve(Responses.requestError("Only understands http POST"));
		try {
			Map<String, String> content = new HashMap<>();
			session.parseBody(content);
			Gson gson = new Gson();
			RpcRequest req = gson.fromJson(content.get("postData"),
					RpcRequest.class);
			RpcResponse resp = getResponse(req);
			return serve(resp);
		} catch (Exception e) {
			return serve(Responses.requestError(e.getMessage()));
		}
	}

	private RpcResponse getResponse(RpcRequest req) {
		RpcMethod method = RpcMethod.of(req);
		if (method == null)
			return Responses.unknownMethod(req);
		switch (method) {
			case INSERT_MODEL:
				return insertModel(req);
			case GET_MODEL:
				return getModel(req);
			default:
				return Responses.unknownMethod(req);
		}
	}

	private Response serve(RpcResponse r) {
		String json = new Gson().toJson(r);
		return newFixedLengthResponse(Response.Status.OK,
				"application/json", json);
	}

	private RpcResponse insertModel(RpcRequest req) {
		BaseDescriptor d = getDescriptor(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with" +
					" valid @id and @type", req);
		JsonObject obj = req.params.getAsJsonObject();
		try {
			MemStore store = new MemStore();
			store.put(d.getModelType(), obj);
			JsonImport imp = new JsonImport(store, db);
			imp.setUpdateMode(UpdateMode.NEVER);
			imp.run(d.getModelType(), d.getRefId());
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	private RpcResponse getModel(RpcRequest req) {
		BaseDescriptor d = getDescriptor(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with" +
					" valid @id and @type", req);
		try {
			RootEntity e = Daos.root(db, d.getModelType()).getForRefId(d.getRefId());
			if (e == null)
				return Responses.error(404, "Not found", req);
			MemStore store = new MemStore();
			JsonExport exp = new JsonExport(db, store);
			exp.setExportReferences(false);
			exp.write(e);
			JsonObject obj = store.get(d.getModelType(), d.getRefId());
			if (obj == null)
				return Responses.error(500, "Conversion to JSON failed", req);
			return Responses.ok(obj, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	private BaseDescriptor getDescriptor(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return null;
		JsonObject obj = req.params.getAsJsonObject();
		return Models.getDescriptor(obj);
	}


}
