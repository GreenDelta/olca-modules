package org.openlca.io.pubchem;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record PropertyValue(JsonObject json) {

	public Integer intValue() {
		var opt = Json.getInt(json, "ival");
		return opt.isPresent() ? opt.getAsInt() : null;
	}

	public Double floatValue() {
		var opt = Json.getDouble(json, "fval");
		return opt.isPresent() ? opt.getAsDouble() : null;
	}

	public String stringValue() {
		return Json.getString(json, "sval");
	}
}

