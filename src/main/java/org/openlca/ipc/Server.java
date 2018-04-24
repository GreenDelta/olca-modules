package org.openlca.ipc;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import fi.iki.elonen.NanoHTTPD;
import org.openlca.core.database.IDatabase;

import java.util.concurrent.atomic.AtomicLong;

public class Server extends NanoHTTPD {

	private final IDatabase db;

	private AtomicLong methodID = new AtomicLong(0L);

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
		RcpResponse r = new RcpResponse();
		r.result = new JsonPrimitive("ok!");
		return serve(r);
	}

	private Response serve(RcpResponse r) {
		String json = new Gson().toJson(r);
		return newFixedLengthResponse(Response.Status.OK,
				"application/json", json);
	}
}
