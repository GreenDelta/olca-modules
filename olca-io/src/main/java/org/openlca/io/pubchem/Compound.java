package org.openlca.io.pubchem;

import java.util.ArrayList;
import java.util.List;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record Compound(JsonObject json) {

	public CompoundId id() {
		var idObj = Json.getObject(json, "id");
		return idObj != null ? new CompoundId(idObj) : null;
	}

	public int charge() {
		return Json.getInt(json, "charge", 0);
	}

	public List<Property> properties() {
		var array = Json.getArray(json, "props");
		if (array == null)
			return List.of();
		var props = new ArrayList<Property>();
		for (var e : array) {
			if (e.isJsonObject()) {
				props.add(new Property(e.getAsJsonObject()));
			}
		}
		return props;
	}
}

