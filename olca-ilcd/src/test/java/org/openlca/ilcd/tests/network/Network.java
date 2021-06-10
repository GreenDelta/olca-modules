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

	static final String ENDPOINT = "http://localhost:8080/resource";
	static final String USER = "admin";
	static final String PASSWORD = "default";

	private static final Logger log = LoggerFactory.getLogger(Network.class);

	private static boolean isAppAlive;
	static {
		var url = ENDPOINT+ "/authenticate/status";
		try  {
			var con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			int responseCode = con.getResponseCode();
			if (responseCode == 200) {
				log.info("can run tests against {}", ENDPOINT);
				isAppAlive = true;
			} else {
				log.info("no tests against {} possible", ENDPOINT);
				isAppAlive = false;
			}
		} catch (Exception e) {
			log.error("no tests against {} possible: {}", url, e.getMessage());
			isAppAlive = false;
		}
	}

	/** Returns true if the soda4LCA instance is accessible for the tests. */
	public static boolean isAppAlive() {
		return isAppAlive;
	}

	/** Creates a new client connection. */
	public static SodaClient createClient() {
		SodaConnection con = new SodaConnection();
		con.url = ENDPOINT;
		con.user = USER;
		con.password = PASSWORD;
		SodaClient client = new SodaClient(con);
		client.connect();
		return client;
	}

}
