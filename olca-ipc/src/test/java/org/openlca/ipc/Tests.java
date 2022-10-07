package org.openlca.ipc;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.services.ServerConfig;

class Tests {

	private static Server server;

	private static Server getServer() {
		if (server == null) {
			var config = ServerConfig.defaultOf(Derby.createInMemory())
					.withDataDir(DataDir.get())
					.withPort(0)
					.get();
			server = new Server(config).withDefaultHandlers();
			server.start();
		}
		return server;
	}

	private static String getUrl() {
		int port = getServer().getListeningPort();
		return "http://localhost:" + port;
	}

	static RpcResponse post(RpcRequest req) {
		var gson = new Gson();
		try {
			byte[] bytes = gson.toJson(req).getBytes(StandardCharsets.UTF_8);
			var url = new URL(getUrl());
			var con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(false);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset", "utf-8");
			con.setRequestProperty("Content-Length", Integer.toString(bytes.length));
			con.setUseCaches(false);
			try (var out = new DataOutputStream(con.getOutputStream())) {
				out.write(bytes);
			}
			try (var reader = new InputStreamReader(
					con.getInputStream(), StandardCharsets.UTF_8)) {
				return gson.fromJson(reader, RpcResponse.class);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
