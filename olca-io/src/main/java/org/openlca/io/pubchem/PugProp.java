package org.openlca.io.pubchem;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record PugProp(JsonObject json) {

	public PugUrn urn() {
		var urnObj = Json.getObject(json, "urn");
		return urnObj != null ? new PugUrn(urnObj) : null;
	}

	public PugValue value() {
		var valueObj = Json.getObject(json, "value");
		return valueObj != null ? new PugValue(valueObj) : null;
	}
}

