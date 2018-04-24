package org.openlca.ipc;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import fi.iki.elonen.NanoHTTPD;
import org.openlca.core.database.IDatabase;

import java.io.InputStreamReader;

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
		switch (method) {
			case "POST":
			case "PUT":
				break;
			default:
				return serve(Responses.requestError(
						"Only understands http POST and PUT"));
		}
		try {
			InputStreamReader reader =
					new InputStreamReader(session.getInputStream(), "utf-8");
			Gson gson = new Gson();
			RpcRequest req = gson.fromJson(reader, RpcRequest.class);
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
		RpcResponse r = new RpcResponse();
		r.id = req.id;
		r.result = new JsonPrimitive("ok!");
		return r;
	}

	private Response serve(RpcResponse r) {
		String json = new Gson().toJson(r);
		return newFixedLengthResponse(Response.Status.OK,
				"application/json", json);
	}
}
