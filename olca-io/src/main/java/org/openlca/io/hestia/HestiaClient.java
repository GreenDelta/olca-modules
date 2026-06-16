package org.openlca.io.hestia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.poi.ss.formula.functions.T;
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
		return getCycle(id, null);
	}

	public Res<Cycle> getCycle(String id, String dataVersion) {
		var json = request("/cycles/" + id, this::asJsonObject, req -> {
			if (Strings.isNotBlank(dataVersion)) {
				req.header("x-data-version", dataVersion);
			}
		});
		return json.isError()
			? json.wrapError("Failed to get Cycle with ID=" + id)
			: Res.ok(new Cycle(json.value()));
	}

	public Res<Site> getSite(String id) {
		return getSite(id, null);
	}

	public Res<Site> getSite(String id, String dataVersion) {
		var json = request("/sites/" + id, this::asJsonObject, req -> {
			if (Strings.isNotBlank(dataVersion)) {
				req.header("x-data-version", dataVersion);
			}
		});
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
		return request(path, this::asJsonArray, req -> {
		});
	}

	private Res<JsonObject> getJsonObject(String path) {
		return request(path, this::asJsonObject, req -> {
		});
	}

	public Res<List<SearchResult>> search(SearchQuery query) {
		if (query == null || Strings.isBlank(query.term()))
			return Res.error("empty search query provided");

		var res = request("/search", this::asJsonObject, req -> {
			req.header("content-type", "application/json");
			if (Strings.isNotBlank(query.dataVersion())) {
				req.header("x-data-version", query.dataVersion());
			}
			var queryJson = query.toJson().toString();
			req.POST(HttpRequest.BodyPublishers.ofString(queryJson));
		});
		if (res.isError())
			return res.wrapError("Search request failed");

		var array = Json.getArray(res.value(), "results");
		if (array == null)
			return Res.error("Search request failed: no results in response");

		var results = new ArrayList<SearchResult>();
		for (var e : array) {
			if (e.isJsonObject()) {
				results.add(new SearchResult(e.getAsJsonObject()));
			}
		}
		return Res.ok(results);
	}

	private <T extends JsonElement> Res<T> request(
		String path,
		Function<JsonElement, Res<T>> converter,
		Consumer<HttpRequest.Builder> decorator
	) {
		try {
			var builder = HttpRequest.newBuilder()
				.uri(URI.create(api + path))
				.header("accept", "application/json")
				.header("x-access-token", apiKey);
			decorator.accept(builder);
			var req = builder.build();

			var resp = http.send(req, BodyHandlers.ofString());
			if (resp.statusCode() != 200) {
				return Res.error("Request failed: "
					+ resp.statusCode() + " - " + resp.body());
			}
			var json = JsonParser.parseString(resp.body());
			var res = converter.apply(json);
			return res.isError()
				? res.wrapError("Request failed: " + req.method() + " " + path)
				: res;
		} catch (Exception e) {
			return Res.error("Request failed: " + path, e);
		}
	}

	private Res<JsonObject> asJsonObject(JsonElement json) {
		return json.isJsonObject()
			? Res.ok(json.getAsJsonObject())
			: Res.error("Returned value is not a JSON object");
	}

	private Res<JsonArray> asJsonArray(JsonElement json) {
		return json.isJsonArray()
			? Res.ok(json.getAsJsonArray())
			: Res.error("Returned value is not a JSON array");
	}

	@Override
	public void close() {
		http.close();
	}

}
