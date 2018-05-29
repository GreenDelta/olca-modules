package org.openlca.ipc;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.ipc.handlers.Calculator;
import org.openlca.jsonld.input.UpdateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public final IDatabase db;
	public final IMatrixSolver solver;
	public final HashMap<String, Object> memory = new HashMap<>();

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
		case CALCULATE:
			return Calculator.doIt(this, req);
		case DISPOSE:
			return dispose(req);
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
		Response resp = newFixedLengthResponse(
				Response.Status.OK, "application/json", json);
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Methods", "POST");
		resp.addHeader("Access-Control-Allow-Headers",
				"Content-Type, Allow-Control-Allow-Headers");
		return resp;
	}

}
