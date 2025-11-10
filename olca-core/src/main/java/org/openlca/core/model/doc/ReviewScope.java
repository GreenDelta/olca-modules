package org.openlca.core.model.doc;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openlca.commons.Copyable;
import org.openlca.commons.Strings;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReviewScope implements Copyable<ReviewScope> {

	public final String name;

	public final Set<String> methods = new HashSet<>();

	public ReviewScope(String name) {
		this.name = name;
	}

	public JsonObject toJson() {
		var obj = new JsonObject();
		Json.put(obj, "name", name);
		var array = new JsonArray(methods.size());
		for (var m : methods) {
			array.add(m);
		}
		Json.put(obj, "methods", array);
		return obj;
	}

	public static Optional<ReviewScope> fromJson(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		var name = Json.getString(obj, "name");
		if (Strings.isBlank(name))
			return Optional.empty();
		var scope = new ReviewScope(name);

		var array = Json.getArray(obj, "methods");
		if (array != null) {
			for (var i : array) {
				if (!i.isJsonPrimitive())
					continue;
				var prim = i.getAsJsonPrimitive();
				if (!prim.isString())
					continue;
				scope.methods.add(prim.getAsString());
			}
		}
		return Optional.of(scope);
	}


	@Override
	public ReviewScope copy() {
		var copy = new ReviewScope(name);
		copy.methods.addAll(methods);
		return copy;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ReviewScope other))
			return false;
		if (!Objects.equals(this.name, other.name))
			return false;
		if (this.methods.size() != other.methods.size())
			return false;
		for (var m : this.methods) {
			if (!other.methods.contains(m))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		if (name != null) {
			hash = 31 * hash + name.hashCode();
		}
		for (var m : methods) {
			if (m != null) {
				hash = 31 * hash + m.hashCode();
			}
		}
		return hash;
	}
}
