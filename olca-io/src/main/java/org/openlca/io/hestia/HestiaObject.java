package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public interface HestiaObject {

	JsonObject json();

	default String id() {
		return Json.getString(json(), "@id");
	}

	default String type() {
		return Json.getString(json(), "@type");
	}

}
