package org.openlca.jsonld;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.DataPackage;
import org.openlca.core.database.DataPackage.DataPackageType;
import org.openlca.util.Strings;

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

	public Set<DataPackage> dataPackages() {
		// support legacy field name
		var array = Json.getArray(json, "libraries");
		if (array == null) {
			array = Json.getArray(json, "dataPackages");
		}
		return Json.stream(array)
				.map(this::parseFrom)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	private Optional<DataPackage> parseFrom(JsonElement json) {
		if (json == null)
			return Optional.empty();

		// for backwards compatibility we accept plain library IDs here
		if (json.isJsonPrimitive()) {
			var prim = json.getAsJsonPrimitive();
			if (!prim.isString())
				return Optional.empty();
			var name = prim.getAsString();
			return name.isBlank()
					? Optional.empty()
					: Optional.of(DataPackage.library(name, null));
		}

		if (!json.isJsonObject())
			return Optional.empty();

		var obj = json.getAsJsonObject();
		// for backwards compatibility we accept library id field as name here
		var name = Json.getString(obj, "id");
		var url = Json.getString(obj, "url");
		if (!Strings.nullOrEmpty(name))
			return Optional.of(DataPackage.library(name, url));
		name = Json.getString(obj, "name");
		if (Strings.nullOrEmpty(name))
			return Optional.empty();
		var type = getDataPackageType(obj);
		if (type == null)
			return Optional.empty();
		var version = Json.getString(obj, "version");
		return Optional.of(new DataPackage(type, name, version, url));
	}

	private DataPackageType getDataPackageType(JsonObject obj) {
		var type = Json.getString(obj, "type");
		for (var t : DataPackageType.values())
			if (t.name().equals(type))
				return t;
		return null;
	}
	
	public PackageInfo withDataPackages(Collection<DataPackage> packages) {
		if (packages == null || packages.isEmpty())
			return this;
		var array = new JsonArray();
		for (var p : packages) {
			array.add(toJson(p));
		}
		json.add("dataPackages", array);
		return this;
	}

	private JsonObject toJson(DataPackage p) {
		var obj = new JsonObject();
		Json.put(obj, "type", p.type().name());
		Json.put(obj, "name", p.name());
		if (p.url() != null) {
			Json.put(obj, "url", p.url());
		}
		if (p.version() != null) {
			Json.put(obj, "version", p.version());
		}
		return obj;
	}

	public PackageInfo withSchemaVersion(SchemaVersion version) {
		if (version == null)
			return this;
		Json.put(json, "schemaVersion", version.value());
		return this;
	}
}
