package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record Release(String version, String name) {

	static Release of(JsonObject obj) {
		return new Release(
			Json.getString(obj, "version"),
			Json.getString(obj, "name"));
	}
}
