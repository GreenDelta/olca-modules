package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartProject(JsonObject json) {

	public SmartProject() {
		this(new JsonObject());
	}

	public SmartProject(JsonObject json) {
		this.json = Objects.requireNonNull(json);
	}

	public static Optional<SmartProject> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		return Optional.of(new SmartProject(obj));
	}

	public static List<SmartProject> allOf(JsonArray array) {
		var projects = new ArrayList<SmartProject>();
		for (var e : array) {
			of(e).ifPresent(projects::add);
		}
		return projects;
	}

	public String id() {
		return Json.getString(json, "id");
	}

	public SmartProject id(String id) {
		Json.put(json, "id", id);
		return this;
	}

	public String name() {
		return Json.getString(json, "name");
	}

	public SmartProject name(String name) {
		Json.put(json, "name", name);
		return this;
	}

	public SmartPcr pcr() {
		return SmartPcr
			.of(Json.getObject(json, "pcr"))
			.orElse(null);
	}

	public SmartProject pcr(SmartPcr pcr) {
		if (pcr != null) {
			Json.put(json, "pcr", pcr.json());
		}
		return this;
	}

	public SmartOrg org() {
		return SmartOrg
			.of(Json.getObject(json, "organization"))
			.orElse(null);
	}

	public SmartProject org(SmartOrg org) {
		if (org != null) {
			Json.put(json, "organization", org.json());
		}
		return this;
	}

	public String image() {
		return Json.getString(json, "image");
	}

	public SmartProject image(String image) {
		Json.put(json, "image", image);
		return this;
	}

	public String phoneNumber() {
		return Json.getString(json, "phone_number");
	}

	public SmartProject phoneNumber(String phoneNumber) {
		Json.put(json, "phone_number", phoneNumber);
		return this;
	}

	public String email() {
		return Json.getString(json, "email");
	}

	public SmartProject email(String email) {
		Json.put(json, "email", email);
		return this;
	}

	public int epdCount() {
		return Json.getInt(json, "epds_count", 0);
	}

	public SmartProject epdCount(int epdCount) {
		Json.put(json, "epds_count", epdCount);
		return this;
	}

}
