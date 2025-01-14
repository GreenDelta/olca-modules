package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/// Describes a reference to an openLCA entity in the indicator and method
/// mappings.
record Ref(String id, String name) {

	static Optional<Ref> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		return Optional.of(new Ref(
				Json.getString(obj, "@id"),
				Json.getString(obj, "name")));
	}

	static List<Ref> allOf(JsonArray array) {
		if (array == null)
			return List.of();
		var refs = new ArrayList<Ref>(array.size());
		for (var el : array) {
			of(el).ifPresent(refs::add);
		}
		return refs;
	}

	JsonObject json() {
		var obj = new JsonObject();
		Json.put(obj, "@id", id);
		Json.put(obj, "name", name);
		return obj;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Ref other))
			return false;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return id != null
				? id.hashCode()
				: System.identityHashCode(this);
	}
}
