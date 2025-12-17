package org.openlca.io.pubchem;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record Property(JsonObject json) {

	public PropertyUrn urn() {
		var urnObj = Json.getObject(json, "urn");
		return urnObj != null ? new PropertyUrn(urnObj) : null;
	}

	public PropertyValue value() {
		var valueObj = Json.getObject(json, "value");
		return valueObj != null ? new PropertyValue(valueObj) : null;
	}
}

