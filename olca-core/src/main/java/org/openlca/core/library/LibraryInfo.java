package org.openlca.core.library;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Contains the meta-data of a library.
 */
public class LibraryInfo {

	private String name;
	private String description;

	/**
	 * Indicates whether this library is regionalized or not. In case of a
	 * regionalized library each element in the elementary flow index can be a
	 * flow-location pair.
	 */
	private boolean isRegionalized;

	/**
	 * A list of library IDs this library depends on.
	 */
	private final Set<String> dependencies = new HashSet<>();

	public static LibraryInfo of(String name) {
		var info = new LibraryInfo();
		info.name = name;
		return info;
	}

	public LibraryInfo name(String name) {
		this.name = name.trim();
		return this;
	}

	public String name() {
		return name;
	}

	public LibraryInfo description(String description) {
		this.description = description;
		return this;
	}

	public String description() {
		return description;
	}

	public Set<String> dependencies() {
		return dependencies;
	}

	public LibraryInfo isRegionalized(boolean b) {
		this.isRegionalized = b;
		return this;
	}

	public boolean isRegionalized() {
		return isRegionalized;
	}

	public void writeTo(Library library) {
		if (library == null)
			return;
		Json.write(toJson(), new File(library.folder(), "library.json"));
	}

	JsonObject toJson() {
		var obj = new JsonObject();
		Json.put(obj, "name", name);
		Json.put(obj, "description", description);
		obj.addProperty("isRegionalized", isRegionalized);
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
		var name = Json.getString(obj, "name");
		var info = LibraryInfo.of(name);
		info.isRegionalized = Json.getBool(obj, "isRegionalized", false);
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
		return Objects.equals(this.name, other.name);
	}

	@Override
	public int hashCode() {
		return name != null
			? name.hashCode()
			: super.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
