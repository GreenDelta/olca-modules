package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record HestiaRef(JsonObject json) implements HestiaObject {

	public String name() {
		return Json.getString(json, "name");
	}

}
