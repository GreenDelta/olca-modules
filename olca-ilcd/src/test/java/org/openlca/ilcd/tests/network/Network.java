package org.openlca.ilcd.tests.network;

import java.net.HttpURLConnection;
import java.net.URL;

import org.openlca.ilcd.io.SodaClient;
import org.openlca.ilcd.io.SodaConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the information for a network connection.
 */
class Network {

	static final String APP_URL = "http://localhost:8080/soda";
	static final String RESOURCE_URL = "http://localhost:8080/soda/resource";
	static final String USER = "admin";
	static final String PASSWORD = "default";

	private static Boolean isAppAlive = null;
	private static Logger log = LoggerFactory.getLogger(Network.class);

	/** Returns true if the soda4LCA instance is accessible for the tests. */
	public static boolean isAppAlive() {
		if (isAppAlive != null)
			return isAppAlive;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(APP_URL)
					.openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			if (responseCode == 200)
				isAppAlive = true;
			else
				noConnection();
		} catch (Exception e) {
			noConnection();
		}
		return isAppAlive;
	}

	private static void noConnection() {
		log.warn("soda4LCA server is not available -> no network tests");
		isAppAlive = false;
	}

	/** Creates a new client connection. */
	public static SodaClient createClient() throws Exception {
		SodaConnection con = new SodaConnection();
		con.url = RESOURCE_URL;
		con.user = USER;
		con.password = PASSWORD;
		SodaClient client = new SodaClient(con);
		client.connect();
		return client;
	}

}
