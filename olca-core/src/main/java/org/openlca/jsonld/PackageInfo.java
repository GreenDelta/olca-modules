package org.openlca.jsonld;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

	public List<LibraryLink> libraries() {
		var array = Json.getArray(json, "libraries");
		if (array == null)
			return Collections.emptyList();
		return Json.stream(array)
				.map(LibraryLink::parseFrom)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
	}

	public PackageInfo withLibraries(Collection<LibraryLink> links) {
		if (links == null || links.isEmpty())
			return this;
		var array = new JsonArray();
		for (var link : links) {
			array.add(link.toJson());
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
