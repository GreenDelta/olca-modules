package org.openlca.git;

import java.util.Collection;
import java.util.List;

import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.jsonld.JsonStoreWriter;
import org.openlca.jsonld.LibraryLink;
import org.openlca.jsonld.PackageInfo;
import org.openlca.jsonld.SchemaVersion;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record RepositoryInfo(JsonObject json) {
	
	public static final String FILE_NAME = PackageInfo.FILE_NAME;

	public static RepositoryInfo of(JsonElement json) {
		var obj = json != null && json.isJsonObject()
				? json.getAsJsonObject()
				: new JsonObject();
		return new RepositoryInfo(obj);
	}

	public static RepositoryInfo create() {
		var json = PackageInfo.create().json();
		Json.put(json, "repositoryVersion", RepositoryVersion.current().value());
		return new RepositoryInfo(json);
	}

	public static RepositoryInfo readFrom(JsonStoreReader reader) {
		var elem = reader.getJson(FILE_NAME);
		return of(elem);
	}

	public void writeTo(JsonStoreWriter writer) {
		writer.put(FILE_NAME, json);
	}

	public SchemaVersion schemaVersion() {
		return PackageInfo.of(json).schemaVersion();
	}

	public List<LibraryLink> libraries() {
		return PackageInfo.of(json).libraries();
	}

	public RepositoryVersion repositoryVersion() {
		var value = Json.getInt(json, "repositoryVersion", RepositoryVersion.fallback().value());
		return new RepositoryVersion(value);
	}

	public RepositoryInfo withLibraries(Collection<LibraryLink> links) {
		var json = PackageInfo.of(this.json).withLibraries(links).json();
		return of(json);
	}

	public RepositoryInfo withSchemaVersion(SchemaVersion version) {
		var json = PackageInfo.of(this.json).withSchemaVersion(version).json();
		return of(json);
	}

	public RepositoryInfo withRepositoryVersion(RepositoryVersion version) {
		if (version == null)
			return this;
		Json.put(json, "repositoryVersion", version.value());
		return this;
	}
}
