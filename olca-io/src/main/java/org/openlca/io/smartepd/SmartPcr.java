package org.openlca.io.smartepd;

import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartPcr(JsonObject json) {

	public static Optional<SmartPcr> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		return Optional.of(new SmartPcr(obj));
	}

	public String id() {
		return Json.getString(json, "id");
	}

	public SmartPcr id(String id) {
		Json.put(json, "id", id);
		return this;
	}

	public String name() {
		return Json.getString(json, "name");
	}

	public SmartPcr name(String name) {
		Json.put(json, "name", name);
		return this;
	}
}
