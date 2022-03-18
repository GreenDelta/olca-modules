package org.openlca.core.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Contains the meta-data of a library.
 */
public class LibraryInfo {

	/**
	 * like https://docs.npmjs.com/files/package.json#name
	 */
	private String name;

	/**
	 * like https://docs.npmjs.com/files/package.json#version
	 */
	private String version;

	/**
	 * like https://docs.npmjs.com/files/package.json#description-1
	 */
	private String description;

	/**
	 * Indicates whether this library is regionalized or not. In case of a
	 * regionalized library each element in the elementary flow index can be
	 * a flow-location pair.
	 */
	private boolean isRegionalized;

	/**
	 * A list of library IDs this library depends on.
	 * <p>
	 * like https://docs.npmjs.com/files/package.json#dependencies
	 */
	private final List<String> dependencies = new ArrayList<>();

	private LibraryInfo(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public static LibraryInfo of(String name, String version) {
		return of(name, Version.fromString(version));
	}

	public static LibraryInfo of(String name, Version version) {
		var n = name.trim();
		return new LibraryInfo(n, version.toString());
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

	public LibraryInfo name(String name) {
		this.name = name.trim();
		return this;
	}

	public String name() {
		return name;
	}

	public LibraryInfo version(String version) {
		this.version = Version.format(version);
		return this;
	}

	public String version() {
		return version;
	}

	public LibraryInfo description(String description) {
		this.description = description;
		return this;
	}

	public String description() {
		return description;
	}

	public List<String> dependencies() {
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
		Json.put(obj, "version", Version.format(version));
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
		var version = Json.getString(obj, "version");
		var info = LibraryInfo.of(name, version);
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
		return Objects.equals(this.id(), other.id());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id());
	}
}
