package org.openlca.ipc;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {

	private final Logger log = LoggerFactory.getLogger(getClass());

	final IDatabase db;
	final HashMap<String, Object> memory = new HashMap<>();
	final IMatrixSolver solver;

	public Server(int port, IDatabase db, IMatrixSolver solver) {
		super(port);
		this.db = db;
		this.solver = solver;
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
			log.info("Started IPC server @{}", port);
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
			RpcRequest req = gson.fromJson(content.get("postData"), RpcRequest.class);
			log.trace("handle request {}/{}", req.id, req.method);
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
			return saveModel(req, UpdateMode.NEVER);
		case UPDATE_MODEL:
			return saveModel(req, UpdateMode.ALWAYS);
		case GET_MODEL:
			return getModel(req);
		case GET_MODELS:
			return getModels(req);
		case GET_DESCRIPTORS:
			return getDescriptors(req);
		case DELETE_MODEL:
			return deleteModel(req);
		default:
			return Responses.unknownMethod(req);
		}
	}

	private Response serve(RpcResponse r) {
		String json = new Gson().toJson(r);
		Response resp = newFixedLengthResponse(Response.Status.OK, "application/json", json);
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Methods", "POST");
		resp.addHeader("Access-Control-Allow-Headers",
				"Content-Type, Allow-Control-Allow-Headers");
		return resp;
	}

	private RpcResponse saveModel(RpcRequest req, UpdateMode mode) {
		BaseDescriptor d = readDescriptor(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with" + " valid @id and @type", req);
		JsonObject obj = req.params.getAsJsonObject();
		try {
			MemStore store = new MemStore();
			store.put(d.getModelType(), obj);
			JsonImport imp = new JsonImport(store, db);
			imp.setUpdateMode(mode);
			imp.run(d.getModelType(), d.getRefId());
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	private RpcResponse getModel(RpcRequest req) {
		BaseDescriptor d = readDescriptor(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with" + " valid @id and @type", req);
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

	@SuppressWarnings("unchecked")
	private <T extends RootEntity> RpcResponse deleteModel(RpcRequest req) {
		BaseDescriptor d = readDescriptor(req);
		if (d == null)
			return Responses.invalidParams("params must be an object with" + " valid @id and @type", req);
		try {
			RootEntityDao<T, ?> dao = (RootEntityDao<T, ?>) Daos.root(db, d.getModelType());
			T e = dao.getForRefId(d.getRefId());
			if (e == null)
				return Responses.error(404, "Not found", req);
			dao.delete(e);
			return Responses.ok(req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	private RpcResponse getModels(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("params must be an object with" + " valid @type attribute", req);
		ModelType type = Models.getType(req.params.getAsJsonObject());
		if (type == null)
			return Responses.invalidParams("params must be an object with" + " valid @type attribute", req);
		try {
			MemStore store = new MemStore();
			JsonExport exp = new JsonExport(db, store);
			exp.setExportReferences(false);
			Daos.root(db, type).getAll().forEach(exp::write);
			JsonArray array = new JsonArray();
			store.getAll(type).forEach(array::add);
			return Responses.ok(array, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	private RpcResponse getDescriptors(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return Responses.invalidParams("params must be an object with" + " valid @type attribute", req);
		ModelType type = Models.getType(req.params.getAsJsonObject());
		if (type == null)
			return Responses.invalidParams("params must be an object with" + " valid @type attribute", req);
		try {
			JsonArray array = new JsonArray();
			Daos.root(db, type).getDescriptors().forEach(d -> {
				JsonObject obj = Json.toJson(d);
				array.add(obj);
			});
			return Responses.ok(array, req);
		} catch (Exception e) {
			return Responses.serverError(e, req);
		}
	}

	private BaseDescriptor readDescriptor(RpcRequest req) {
		if (req.params == null || !req.params.isJsonObject())
			return null;
		JsonObject obj = req.params.getAsJsonObject();
		return Models.getDescriptor(obj);
	}
}
