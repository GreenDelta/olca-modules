package org.openlca.ipc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.services.ServerConfig;

class Tests {

	private static Server server;

	private static Server getServer() {
		if (server == null) {
			server = new Server(0);
			var config = new ServerConfig(
					DataDir.get(),
					Derby.createInMemory(),
					0,
					false,
					null,
					Collections.emptyMap());
			server.withDefaultHandlers(config);
			server.start();
		}
		return server;
	}

	private static String getUrl() {
		int port = getServer().getListeningPort();
		return "http://localhost:" + port;
	}

	static String post(String data) {
		try {
			byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
			URL url = new URL(getUrl());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(false);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("charset", "utf-8");
			con.setRequestProperty("Content-Length", Integer.toString(bytes.length));
			con.setUseCaches(false);
			try (DataOutputStream out = new DataOutputStream(
					con.getOutputStream())) {
				out.write(bytes);
			}
			try (Reader in = new BufferedReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				StringBuilder sb = new StringBuilder();
				for (int c; (c = in.read()) >= 0;)
					sb.append((char) c);
				return sb.toString();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
