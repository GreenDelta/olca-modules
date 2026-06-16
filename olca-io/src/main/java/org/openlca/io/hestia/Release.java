package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record Release(JsonObject json) {

	public String version() {
		return Json.getString(json, "version");
	}

	public String name() {
		return Json.getString(json, "name");
	}
}
