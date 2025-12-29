package org.openlca.io.pubchem;

import java.util.ArrayList;
import java.util.List;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

/// View data returned by the PUG View API.
public record PugView(JsonObject json) {

	public List<PugViewRef> references() {
		var array = Json.getArray(json, "Reference");
		if (array == null)
			return List.of();
		var refs = new ArrayList<PugViewRef>();
		for (var e : array) {
			if (e.isJsonObject()) {
				refs.add(new PugViewRef(e.getAsJsonObject()));
			}
		}
		return refs;
	}

	public String cas() {
		for (var ref : references()) {
			if ("CAS Common Chemistry".equals(ref.sourceName())) {
				return ref.sourceId();
			}
		}
		return null;
	}
}
