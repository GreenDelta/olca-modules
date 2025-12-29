package org.openlca.io.pubchem;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record PugValue(JsonObject json) {

	public Integer getInt() {
		var opt = Json.getInt(json, "ival");
		return opt.isPresent() ? opt.getAsInt() : null;
	}

	public Double getFloat() {
		var opt = Json.getDouble(json, "fval");
		return opt.isPresent() ? opt.getAsDouble() : null;
	}

	public String getString() {
		return Json.getString(json, "sval");
	}
}

