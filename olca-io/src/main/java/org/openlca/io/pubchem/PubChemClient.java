package org.openlca.io.pubchem;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PubChemClient implements AutoCloseable {

	public static String DEFAULT_API = "https://pubchem.ncbi.nlm.nih.gov/rest/pug";

	private final String api;
	private final HttpClient http;

	private PubChemClient(String api) {
		this.api = api;
		this.http = HttpClient.newHttpClient();
	}

	public static PubChemClient create() {
		return new PubChemClient(DEFAULT_API);
	}

	public static PubChemClient create(String api) {
		var url = api.strip();
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return new PubChemClient(url);
	}

	public Res<List<PugCompound>> getCompoundsByName(String name) {
		if (Strings.isBlank(name))
			return Res.error("empty compound name provided");

		try {
			var encodedName = java.net.URLEncoder.encode(name.strip(), StandardCharsets.UTF_8);
			var path = "/compound/name/" + encodedName + "/JSON";
			var req = HttpRequest.newBuilder()
				.uri(URI.create(api + path))
				.header("accept", "application/json")
				.build();

			var json = fetchJsonObject(req);
			if (json.isError())
				return json.wrapError("failed to get compounds for: " + name);

			var array = Json.getArray(json.value(), "PC_Compounds");
			if (array == null)
				return Res.error("response does not contain PC_Compounds array");

			var compounds = new ArrayList<PugCompound>();
			for (var e : array) {
				if (e.isJsonObject()) {
					compounds.add(new PugCompound(e.getAsJsonObject()));
				}
			}
			return Res.ok(compounds);
		} catch (Exception e) {
			return Res.error("request failed for compound: " + name, e);
		}
	}

	private Res<JsonObject> fetchJsonObject(HttpRequest req) {
		try {
			var resp = http.send(req, BodyHandlers.ofString());
			if (resp.statusCode() != 200) {
				return Res.error("request failed: "
					+ resp.statusCode() + " - " + resp.body());
			}
			var json = JsonParser.parseString(resp.body());
			return json.isJsonObject()
				? Res.ok(json.getAsJsonObject())
				: Res.error("response is not a JSON object");
		} catch (Exception e) {
			return Res.error("request failed", e);
		}
	}

	@Override
	public void close() {
		http.close();
	}
}
