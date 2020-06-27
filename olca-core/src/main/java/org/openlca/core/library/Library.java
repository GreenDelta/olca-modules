package org.openlca.core.library;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.util.Pair;

public class Library {

	/**
	 * like https://docs.npmjs.com/files/package.json#name
	 */
	public final String name;

	/**
	 * like https://docs.npmjs.com/files/package.json#version
	 */
	public final String version;

	/**
	 * like https://docs.npmjs.com/files/package.json#description-1
	 */
	public String description;

	/**
	 * Indicates whether this library is regionalized or not. In case of a
	 * regionalized library each element in the elementary flow index can be
	 * a flow-location pair.
	 */
	public boolean isRegionalized;

	/**
	 * A list of [name, version] pairs of libraries on which this library depends.
	 *
	 * like https://docs.npmjs.com/files/package.json#dependencies
	 */
	public final List<Pair<String, String>> dependencies = new ArrayList<>();

	public Library(String name, String version) {
		this.name = name;
		this.version = version;
	}

	/**
	 * The identifier of a library is the combination of `[name]_[version]`.
	 * The identifier of a library must be unique. It is up to the user to
	 * find good library names (specifying the type of allocation,
	 * regionalization etc.). However, there are restrictions on the name
	 * as we also use them as folder names.
	 */
	public String id() {
		return (name + "_" + Version.format(version)).trim().toLowerCase();
	}

	JsonObject toJson() {
		var obj = new JsonObject();
		Json.put(obj, "name", name);
		Json.put(obj, "version", Version.format(version));
		Json.put(obj, "description", description);
		obj.addProperty("isRegionalized", isRegionalized);
		if (dependencies.isEmpty())
			return obj;
		var deps = new JsonObject();
		for (var dep : dependencies) {
			if (dep.first == null || dep.second == null)
				continue;
			deps.addProperty(dep.first, dep.second);
		}
		obj.add("dependencies", deps);
		return obj;
	}

	static Library fromJson(JsonObject obj) {
		var name = Json.getString(obj, "name");
		var version = Version.format(Json.getString(obj, "version"));
		var library = new Library(name, version);
		library.isRegionalized = Json.getBool(obj, "isRegionalized", false);
		var deps = Json.getObject(obj, "dependencies");
		if (deps != null) {
			for (var entry : deps.entrySet()) {
				var v = entry.getValue();
				if (v == null || !v.isJsonPrimitive())
					continue;
				library.dependencies.add(
						Pair.of(entry.getKey(), v.getAsString()));
			}
		}

		return library;
	}
}
