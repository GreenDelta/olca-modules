package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record GlossaryFileInfo(JsonObject json) implements HestiaObject {

	public String termType() {
		return Json.getString(json, "termType");
	}

	public String filename() {
		return Json.getString(json, "filename");
	}

	public String filepath() {
		return Json.getString(json, "filepath");
	}

	public String description() {
		return Json.getString(json, "description");
	}

}
