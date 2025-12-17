package org.openlca.io.pubchem;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record CompoundId(JsonObject json) {

	public int cid() {
		var idObj = Json.getObject(json, "id");
		return idObj != null ? Json.getInt(idObj, "cid", 0) : 0;
	}
}

