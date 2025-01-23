package org.openlca.io.smartepd;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Objects;

import org.openlca.util.Res;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SmartEpdClient {

	private final String url;
	private final String apiKey;
	private final HttpClient client;

	private SmartEpdClient(String url, String apiKey) {
		this.url = Objects.requireNonNull(url);
		this.apiKey = Objects.requireNonNull(apiKey);
		this.client = HttpClient.newBuilder().build();
	}

	public static SmartEpdClient of(String url, String apiKey) {
		var base = url.strip();
		if (!base.endsWith("/")) {
			base += "/";
		}
		if (!base.endsWith("/api/")) {
			base += "api/";
		}
		return new SmartEpdClient(base, apiKey);
	}

	// region: projects

	public Res<List<SmartProject>> getProjects() {
		var res = new Req("public/projects")
				.sendReadJsonArray();
		return res.hasError()
				? res.wrapError("failed to get projects")
				: Res.of(SmartProject.allOf(res.value()));
	}

	public Res<SmartProject> getProject(String id) {
		var res = new Req("public/projects/" + id)
				.sendReadJsonObject();
		return res.hasError()
				? res.wrapError("failed to get project: " + id)
				: Res.of(new SmartProject(res.value()));
	}

	/// Creates a new project.
	public Res<SmartProject> postProject(SmartProject project) {
		if (project == null)
			return Res.error("project is null");
		var res = new Req("public/projects")
				.post(project.json())
				.sendReadJsonObject();
		return res.hasError()
				? res.wrapError("failed to create project")
				: Res.of(new SmartProject(res.value()));
	}

	/// Updates an existing project.
	public Res<SmartProject> putProject(SmartProject project) {
		if (project == null)
			return Res.error("project is null");
		var res = new Req("public/projects/" + project.id())
				.put(project.json())
				.sendReadJsonObject();
		return res.hasError()
				? res.wrapError("failed to update project")
				: Res.of(new SmartProject(res.value()));
	}

	public Res<Void> deleteProject(String id) {
		var res = new Req("public/projects/" + id)
				.delete()
				.send();
		return res.hasError()
				? res.wrapError("failed to delete project: " + id)
				: Res.VOID;
	}

	// endregion

	// region: epds

	public Res<List<SmartEpd>> getEpds(String projectId) {
		var res = new Req("public/projects/" + projectId + "/epds")
				.sendReadJsonArray();
		return res.hasError()
				? res.wrapError("failed to get EPDs of project: " + projectId)
				: Res.of(SmartEpd.allOf(res.value()));
	}

	public Res<SmartEpd> getEpd(String id) {
		var res = new Req("public/epds/" + id)
				.sendReadJsonObject();
		return res.hasError()
				? res.wrapError("failed to get EPD: " + id)
				: Res.of(new SmartEpd(res.value()));
	}

	/// Creates a new EPD.
	public Res<SmartEpd> postEpd(SmartEpd epd) {
		if (epd == null)
			return Res.error("EPD is null");
		var res = new Req("public/epds")
				.post(epd.json())
				.sendReadJsonObject();
		return res.hasError()
				? res.wrapError("failed to create EPD")
				: Res.of(new SmartEpd(res.value()));
	}

	/// Updates an existing EPD.
	public Res<SmartEpd> putEpd(SmartEpd epd) {
		if (epd == null)
			return Res.error("EPD is null");
		var res = new Req("public/epds/" + epd.id())
				.patch(epd.json())
				.sendReadJsonObject();
		return res.hasError()
				? res.wrapError("failed to update EPD")
				: Res.of(new SmartEpd(res.value()));
	}

	public Res<Void> deleteEpd(String id) {
		var res = new Req("public/epds/" + id)
				.delete()
				.send();
		return res.hasError()
				? res.wrapError("failed to delete EPD: " + id)
				: Res.VOID;
	}

	// endregion

	private class Req {

		private HttpRequest.Builder r;

		Req(String path) {
			r = HttpRequest.newBuilder(URI.create(url + path))
					.header("apiKey", apiKey);
		}

		Req delete() {
			r = r.DELETE();
			return this;
		}

		Req post(JsonObject obj) {
			var json = new Gson().toJson(obj);
			r = r.POST(HttpRequest.BodyPublishers.ofString(json))
					.header("Content-Type", "application/json");
			return this;
		}

		Req put(JsonObject obj) {
			var json = new Gson().toJson(obj);
			r = r.PUT(HttpRequest.BodyPublishers.ofString(json))
					.header("Content-Type", "application/json");
			return this;
		}

		Req patch(JsonObject obj) {
			var json = new Gson().toJson(obj);
			r = r.method("PATCH", HttpRequest.BodyPublishers.ofString(json))
					.header("Content-Type", "application/json");
			return this;
		}

		Res<JsonObject> sendReadJsonObject() {
			var res = sendReadJson();
			if (res.hasError())
				return res.castError();
			var json = res.value();
			return json.isJsonObject()
					? Res.of(json.getAsJsonObject())
					: Res.error("returned JSON is not an object");
		}

		Res<JsonArray> sendReadJsonArray() {
			var res = sendReadJson();
			if (res.hasError())
				return res.castError();
			var json = res.value();
			return json.isJsonArray()
					? Res.of(json.getAsJsonArray())
					: Res.error("returned JSON is not an array");
		}

		private Res<JsonElement> sendReadJson() {
			var res = send();
			if (res.hasError())
				return res.castError();
			try {
				var json = new Gson().fromJson(res.value(), JsonElement.class);
				return Res.of(json);
			} catch (Exception e) {
				return Res.error("failed to parse JSON from response", e);
			}
		}

		Res<String> send() {
			try {
				var req = r.build();
				var resp = client.send(req, BodyHandlers.ofString());
				if (resp.statusCode() >= 300) {
					return Res.error("request failed with status " + resp.statusCode()
							+ ": " + resp.body());
				}
				return Res.of(resp.body());
			} catch (Exception e) {
				return Res.error("request failed", e);
			}
		}
	}
}
