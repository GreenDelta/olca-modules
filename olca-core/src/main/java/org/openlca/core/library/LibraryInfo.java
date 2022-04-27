package org.openlca.core.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.util.Strings;

/**
 * Contains the meta-data of a library.
 */
public class LibraryInfo {

	private String name;
	private String version;
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
	private final List<String> dependencies = new ArrayList<>();

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

	public LibraryInfo version(String version) {
		this.version = version;
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
		Json.put(obj, "version", version);
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

	/**
	 * Tries to extract the version from the ID and separate it from the name.
	 */
	static LibraryInfo fromId(String id) {
		String name = id;
		String version = null;
		var sepIdx = id.lastIndexOf(' ');
		if (sepIdx > 0) {
			var v = id.substring(sepIdx + 1);
			if (v.matches("\\d?\\d(\\.\\d\\d?)?(\\.\\d\\d?\\d?)?")) {
				name = id.substring(0, sepIdx);
				version = v;
			}
		}
		return LibraryInfo.of(name).version(version);
	}

	static LibraryInfo fromJson(JsonObject obj) {
		var name = Json.getString(obj, "name");
		var info = LibraryInfo.of(name);
		info.version = Json.getString(obj, "version");
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

	public String toId() {
		return Strings.notEmpty(version)
			? name + " " + version
			: name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var other = (LibraryInfo) o;
		return Objects.equals(this.name, other.name)
				&& Objects.equals(this.version, other.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, version);
	}

	@Override
	public String toString() {
		return toId();
	}
}
