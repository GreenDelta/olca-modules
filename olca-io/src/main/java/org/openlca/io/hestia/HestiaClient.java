package org.openlca.io.hestia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HestiaClient implements AutoCloseable {

	public static String DEFAULT_API = "https://api.hestia.earth";

	private final String api;
	private final String apiKey;
	private final HttpClient http;

	private HestiaClient(String api, String apiKey) {
		this.api = Objects.requireNonNull(api);
		this.apiKey = Objects.requireNonNull(apiKey);
		this.http = HttpClient.newHttpClient();
	}

	public static HestiaClient of(String apiKey) {
		return new HestiaClient(DEFAULT_API, apiKey.strip());
	}

	public static HestiaClient of(String api, String apiKey) {
		var url = api.strip();
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return new HestiaClient(url, apiKey.strip());
	}

	public Res<Cycle> getCycle(String id) {
		var json = getJsonObject("/cycles/" + id);
		return json.isError()
			? json.wrapError("requesting cycle " + id + " failed")
			: Res.ok(new Cycle(json.value()));
	}

	public Res<Site> getSite(String id) {
		var json = getJsonObject("/sites/" + id);
		return json.isError()
			? json.wrapError("requesting site " + id + " failed")
			: Res.ok(new Site(json.value()));
	}

	public Res<HestiaSource> getSource(String id) {
		var json = getJsonObject("/sources/" + id);
		return json.isError()
			? json.wrapError("requesting source " + id + " failed")
			: Res.ok(new HestiaSource(json.value()));
	}

	public Res<User> getCurrentUser() {
		var json = getJsonObject("/users/me");
		return json.isError()
			? json.wrapError("failed to get the current user")
			: Res.ok(new User(json.value()));
	}

	private Res<JsonObject> getJsonObject(String path) {
		try {
			var req = HttpRequest.newBuilder()
				.uri(URI.create(api + path))
				.header("accept", "application/json")
				.header("x-access-token", apiKey)
				.build();
			return fetchJsonObject(req);
		} catch (Exception e) {
			return Res.error("requesting " + path + " failed", e);
		}
	}

	public Res<List<SearchResult>> search(SearchQuery query) {
		if (query == null || Strings.isBlank(query.term()))
			return Res.error("empty search query provided");

		try {

			var queryJson = query.toJson().toString();
			var req = HttpRequest.newBuilder()
				.uri(URI.create(api + "/search"))
				.header("accept", "application/json")
				.header("content-type", "application/json")
				.header("x-access-token", apiKey)
				.POST(HttpRequest.BodyPublishers.ofString(queryJson))
				.build();

			var json = fetchJsonObject(req);
			if (json.isError())
				return json.wrapError("search failed");
			var array = Json.getArray(json.value(), "results");
			if (array == null)
				return Res.error("response does not contain results array");

			var results = new ArrayList<SearchResult>();
			for (var e : array) {
				if (e.isJsonObject()) {
					results.add(new SearchResult(e.getAsJsonObject()));
				}
			}
			return Res.ok(results);
		} catch (Exception e) {
			return Res.error("search request failed", e);
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
