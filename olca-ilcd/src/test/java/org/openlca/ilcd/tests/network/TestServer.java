package org.openlca.ilcd.tests.network;

import java.net.HttpURLConnection;
import java.net.URL;

import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.slf4j.LoggerFactory;

class TestServer {

	static final String ENDPOINT = "http://localhost:8080/resource";
	static final String USER = "admin";
	static final String PASSWORD = "default";

	private static boolean available;
	static {
		var log = LoggerFactory.getLogger(TestServer.class);
		var url = ENDPOINT + "/authenticate/status";
		try {
			var con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			int responseCode = con.getResponseCode();
			if (responseCode == 200) {
				log.info("can run tests against {}", ENDPOINT);
				available = true;
			} else {
				log.info("no tests against {} possible", ENDPOINT);
				available = false;
			}
		} catch (Exception e) {
			log.error("no tests against {} possible: {}", url, e.getMessage());
			available = false;
		}
	}

	/**
	 * Returns true if the soda4LCA instance is accessible for the tests.
	 */
	public static boolean isAvailable() {
		return available;
	}

	/**
	 * Creates a new client connection.
	 */
	public static SodaClient newClient() {
		var con = new SodaConnection();
		con.url = ENDPOINT;
		con.user = USER;
		con.password = PASSWORD;
		var client = new SodaClient(con);
		client.connect();
		return client;
	}
}
