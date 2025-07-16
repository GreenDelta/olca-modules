package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record Site(JsonObject json) implements HestiaObject {

	public String name() {
		return Json.getString(json, "name");
	}

	public String siteType() {
		return Json.getString(json, "siteType");
	}

	public Term country() {
		var obj = Json.getObject(json, "country");
		return obj != null
			? new Term(obj)
			: null;
	}

}
