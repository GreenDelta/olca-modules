package org.openlca.jsonld;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

public record PackageInfo(JsonObject json) {

	public static final String FILE_NAME = "openlca.json";

	public static PackageInfo of(JsonElement json) {
		var obj = json != null && json.isJsonObject()
			? json.getAsJsonObject()
			: new JsonObject();
		return new PackageInfo(obj);
	}

	public static PackageInfo create() {
		var json = new JsonObject();
		Json.put(json, "schemaVersion", SchemaVersion.CURRENT);
		return new PackageInfo(json);
	}

	public static PackageInfo readFrom(JsonStoreReader reader) {
		var elem = reader.getJson(FILE_NAME);
		return of(elem);
	}

	public void writeTo(JsonStoreWriter writer) {
		writer.put(FILE_NAME, json);
	}

	public SchemaVersion schemaVersion() {
		var value = Json.getInt(json, "schemaVersion", SchemaVersion.FALLBACK);
		return new SchemaVersion(value);
	}

	public List<String> libraries() {
		var array = Json.getArray(json, "libraries");
		if (array == null)
			return Collections.emptyList();
		return Json.stream(array)
			.filter(JsonElement::isJsonPrimitive)
			.map(JsonElement::getAsString)
			.toList();
	}

	public PackageInfo withLibraries(Iterable<String> libraryIds) {
		if (libraryIds == null)
			return this;
		var array = new JsonArray();
		for (var libId : libraryIds) {
			array.add(libId);
		}
		json.add("libraries", array);
		return this;
	}

	public PackageInfo withLibraries(String... libraryIds) {
		if (libraryIds == null || libraryIds.length == 0)
			return this;
		var array = new JsonArray();
		for (var libId : libraryIds) {
			array.add(libId);
		}
		json.add("libraries", array);
		return this;
	}

	public PackageInfo withSchemaVersion(SchemaVersion version) {
		if (version == null)
			return this;
		Json.put(json, "schemaVersion", version.value());
		return this;
	}

}
