package org.openlca.core.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;

/**
 * Contains the meta-data of a library.
 */
public class LibraryInfo {

	/**
	 * like https://docs.npmjs.com/files/package.json#name
	 */
	public String name;

	/**
	 * like https://docs.npmjs.com/files/package.json#version
	 */
	public String version;

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
	 * A list of library IDs this library depends on.
	 * <p>
	 * like https://docs.npmjs.com/files/package.json#dependencies
	 */
	public final List<String> dependencies = new ArrayList<>();

	/**
	 * Indicates whether the library has uncertainty information.
	 */
	public boolean hasUncertaintyData;

	public static LibraryInfo of(String name, String version) {
		var info = new LibraryInfo();
		info.name = name;
		info.version = version;
		return info;
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

	public void writeTo(Library library) {
		if (library == null)
			return;
		Json.write(toJson(), new File(library.folder, "library.json"));
	}

	JsonObject toJson() {
		var obj = new JsonObject();
		Json.put(obj, "name", name);
		Json.put(obj, "version", Version.format(version));
		Json.put(obj, "description", description);
		obj.addProperty("isRegionalized", isRegionalized);
		obj.addProperty("hasUncertaintyData", hasUncertaintyData);
		if (dependencies.isEmpty())
			return obj;
		var deps = new JsonArray();
		for (var dep : dependencies) {
			deps.add(dep);
		}
		obj.add("dependencies", deps);
		return obj;
	}

	static LibraryInfo fromJson(JsonObject obj) {
		var info = new LibraryInfo();
		info.name = Json.getString(obj, "name");
		info.version = Version.format(Json.getString(obj, "version"));
		info.isRegionalized = Json.getBool(obj, "isRegionalized", false);
		info.hasUncertaintyData = Json.getBool(obj, "hasUncertaintyData", false);
		var deps = Json.getArray(obj, "dependencies");
		if (deps != null) {
			Json.stream(deps)
				.filter(JsonElement::isJsonPrimitive)
				.map(JsonElement::getAsString)
				.forEach(info.dependencies::add);
		}
		return info;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var other = (LibraryInfo) o;
		return Objects.equals(this.id(), other.id());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id());
	}
}
