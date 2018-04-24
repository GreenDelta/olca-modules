package org.openlca.ipc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import org.openlca.core.database.IDatabase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
		JsonObject obj = new JsonObject();
		obj.addProperty("jsonrpc", "2.0");
		byte[] data = new Gson().toJson(obj).getBytes("utf-8");
		InputStream stream = new ByteArrayInputStream(data);
		return newFixedLengthResponse(Response.Status.OK, "application/json",
				stream, data.length);
	}
}