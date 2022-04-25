package org.openlca.io.openepd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

public class Ec3Client {

	private final String url;
	private final String epdUrl;
	private final String token;
	private final HttpClient http;

	private Ec3Client(HttpClient http, Ec3Credentials creds) {
		this.http = http;
		this.url = formatUrl(creds.ec3Url());
		this.epdUrl = creds.epdUrl() != null
			? formatUrl(creds.epdUrl())
			: this.url;
		this.token = creds.token();
	}

	private static String formatUrl(String url) {
		return !url.endsWith("/")
			? url + "/"
			: url;
	}

	/**
	 * Returns the access token of this client connection.
	 */
	public String token() {
		return token;
	}

	/**
	 * Makes a http get request using the EPD URL. If there is no specific
	 * EPD URL defined, this is the same as calling the `get` method of
	 * this class.
	 */
	public Ec3Response getEpd(String id) {
		return internalGet("epds/" + id, epdUrl);
	}

	public Ec3Response get(String path) {
		return internalGet(path, url);
	}

	private Ec3Response internalGet(String path, String endpoint) {
		var p = path.startsWith("/")
			? path.substring(1)
			: path;
		var req = HttpRequest.newBuilder()
			.uri(URI.create(endpoint + p))
			.header("Accept", "application/json")
			.header("Authorization", "Bearer " + token)
			.GET()
			.build();
		try {
			var resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
			return Ec3Response.of(resp);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException("GET " + path + " failed", e);
		}
	}

	public Ec3Response putEpd(String id, JsonObject epdDoc) {
		try {
			var json = new GsonBuilder()
				.serializeNulls()
				.create()
				.toJson(epdDoc);
			var bodyStr = HttpRequest.BodyPublishers.ofString(
				json, StandardCharsets.UTF_8);
			var req = HttpRequest.newBuilder()
				.uri(URI.create(epdUrl + "epds/" + id))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("Accept", "application/json")
				.PUT(bodyStr)
				.build();
			var resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
			return Ec3Response.of(resp);
		} catch (Exception e) {
			throw new RuntimeException("Failed to post to EC3", e);
		}
	}

	public Ec3Response postEpd(JsonObject epdDoc) {
		try {
			var bodyStr = HttpRequest.BodyPublishers.ofString(
				new Gson().toJson(epdDoc), StandardCharsets.UTF_8);
			var req = HttpRequest.newBuilder()
				.uri(URI.create(epdUrl + "epds"))
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + token)
				.header("Accept", "application/json")
				.POST(bodyStr)
				.build();
			var resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
			return Ec3Response.of(resp);
		} catch (Exception e) {
			throw new RuntimeException("Failed to post to EC3", e);
		}
	}

	public boolean logout() {
		var req = HttpRequest.newBuilder()
			.uri(URI.create(url + "rest-auth/logout"))
			.header("Authorization", "Bearer " + token)
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();
		try {
			var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
			return resp.statusCode() == 200;
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException("POST rest-auth/logout failed", e);
		}
	}

	/**
	 * Tries to connect to the EC3 API using the token from the given credentials.
	 * If this works an EC3 client connection is returned, otherwise the returned
	 * option is empty.
	 */
	public static Optional<Ec3Client> tryToken(Ec3Credentials cred) {
		if (cred == null
			|| Strings.nullOrEmpty(cred.ec3Url())
			|| Strings.nullOrEmpty(cred.token()))
			return Optional.empty();
		try {
			var client = new Ec3Client(http(), cred);
			var resp = client.get("users/me");
			return resp.isOk() && resp.hasJson()
				? Optional.of(client)
				: Optional.empty();
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Ec3Client.class);
			log.error("failed to connect to EC3 API @" + cred.epdUrl(), e);
			return Optional.empty();
		}
	}

	/**
	 * Tries to connect to the EC3 API using the username and password from the
	 * given credentials. If this works an EC3 client connection is returned,
	 * otherwise the returned option is empty. The given credentials are updated
	 * to contain a new access token.
	 */
	public static Optional<Ec3Client> tryLogin(Ec3Credentials cred) {
		if (cred == null
			|| Strings.nullOrEmpty(cred.ec3Url())
			|| Strings.nullOrEmpty(cred.user())
			|| Strings.nullOrEmpty(cred.password()))
			return Optional.empty();

		var log = LoggerFactory.getLogger(Ec3Client.class);
		try {

			var obj = new JsonObject();
			obj.addProperty("username", cred.user());
			obj.addProperty("password", cred.password());
			var json = new Gson().toJson(obj);

			var url = formatUrl(cred.ec3Url());
			var req = HttpRequest.newBuilder()
				.uri(URI.create(url + "rest-auth/login"))
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
				.build();

			log.info("try to login {}@{}", cred.user(), cred.ec3Url());
			var http = http();
			var raw = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
			var resp = Ec3Response.of(raw);
			if (resp.isError() || !resp.hasJson() || !resp.json().isJsonObject()) {
				log.error("login error; status = {}", resp.status());
				return Optional.empty();
			}

			var respObj = resp.json().getAsJsonObject();
			var token = Json.getString(respObj, "key");
			if (Strings.nullOrEmpty(token)) {
				log.error("login ok but  received no token; status = {}", resp.status());
				return Optional.empty();
			}

			cred.token(token);
			return Optional.of(new Ec3Client(http, cred));

		} catch (Exception e) {
			log.error("failed to connect to EC3 API @" + cred.ec3Url(), e);
			return Optional.empty();
		}
	}

	private static HttpClient http() {
		return HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.build();
	}

}
