package org.openlca.io.pubchem;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record PropertyUrn(JsonObject json) {

	public String label() {
		return Json.getString(json, "label");
	}

	public String name() {
		return Json.getString(json, "name");
	}

	public int datatype() {
		return Json.getInt(json, "datatype", 0);
	}

	public String implementation() {
		return Json.getString(json, "implementation");
	}

	public String version() {
		return Json.getString(json, "version");
	}

	public String software() {
		return Json.getString(json, "software");
	}

	public String source() {
		return Json.getString(json, "source");
	}

	public String release() {
		return Json.getString(json, "release");
	}
}

