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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

	public Res<List<Release>> getReleases() {
		var res = getJsonArray("/users/me/releases");
		if (res.isError())
			return res.wrapError("Failed to get the enabled releases");
		var array = res.value();
		var releases = new ArrayList<Release>(array.size());
		for (var e : array) {
			if (e.isJsonObject()) {
				releases.add(new Release(e.getAsJsonObject()));
			}
		}
		return Res.ok(releases);
	}

	public Res<List<GlossaryFileInfo>> getGlossaryFileInfos() {
		var json = getJsonObject("/glossary/lookups");
		if (json.isError())
			return json.wrapError("Failed to get glossary lookup information");
		var array = Json.getArray(json.value(), "results");
		if (array == null)
			return Res.error("No glossary lookup information found in response");
		var infos = new ArrayList<GlossaryFileInfo>(array.size());
		for (var e : array) {
			if (e.isJsonObject()) {
				infos.add(new GlossaryFileInfo(e.getAsJsonObject()));
			}
		}
		return Res.ok(infos);
	}

	private Res<JsonArray> getJsonArray(String path) {
		var res = getJson(path);
		if (res.isError())
			return res.castError();
		var json = res.value();
		return json.isJsonArray()
			? Res.ok(json.getAsJsonArray())
			: Res.error("Returned response is not a JSON array: GET " + path);
	}

	private Res<JsonObject> getJsonObject(String path) {
		var res = getJson(path);
		if (res.isError())
			return res.castError();
		var json = res.value();
		return json.isJsonObject()
			? Res.ok(json.getAsJsonObject())
			: Res.error("Returned response is not a JSON object: GET " + path);
	}

	private Res<JsonElement> getJson(String path) {
		try {
			var req = HttpRequest.newBuilder()
				.uri(URI.create(api + path))
				.header("accept", "application/json")
				.header("x-access-token", apiKey)
				.build();
			return fetchJson(req);
		} catch (Exception e) {
			return Res.error("Request failed: GET " + path, e);
		}
	}

	public Res<List<SearchResult>> search(SearchQuery query) {
		if (query == null || Strings.isBlank(query.term()))
			return Res.error("empty search query provided");

		try {

			var queryJson = query.toJson().toString();
			var builder = HttpRequest.newBuilder()
				.uri(URI.create(api + "/search"))
				.header("accept", "application/json")
				.header("content-type", "application/json")
				.header("x-access-token", apiKey);
			if (Strings.isNotBlank(query.dataVersion())) {
				builder.header("x-data-version", query.dataVersion());
			}
			var req = builder
				.POST(HttpRequest.BodyPublishers.ofString(queryJson))
				.build();

			var res = fetchJson(req);
			if (res.isError())
				return res.wrapError("Search failed");
			var json = res.value();
			if (!json.isJsonObject())
				return Res.error("Search failed: response is not a JSON object");
			var array = Json.getArray(json.getAsJsonObject(), "results");
			if (array == null)
				return Res.error("Response does not contain results array");

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

	private Res<JsonElement> fetchJson(HttpRequest req) {
		try {
			var resp = http.send(req, BodyHandlers.ofString());
			if (resp.statusCode() != 200) {
				return Res.error("Request failed: "
					+ resp.statusCode() + " - " + resp.body());
			}
			var json = JsonParser.parseString(resp.body());
			return Res.ok(json);
		} catch (Exception e) {
			return Res.error("Request failed", e);
		}
	}

	@Override
	public void close() {
		http.close();
	}

}
