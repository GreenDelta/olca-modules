package org.openlca.core.model.doc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Copyable;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewScope implements Copyable<ReviewScope> {

	public final String name;

	public final List<String> methods = new ArrayList<>();

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
		if (Strings.nullOrEmpty(name))
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
}
