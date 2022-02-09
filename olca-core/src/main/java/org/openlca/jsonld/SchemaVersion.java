package org.openlca.jsonld;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SchemaVersion(int value) {

	public static final String FILE_NAME = "olca-schema.json";
	private static final int FALLBACK = 1;
	private static final int CURRENT = 2;

	/**
	 * Get the current schema version that is supported by this API.
	 */
	public static SchemaVersion current() {
		return new SchemaVersion(CURRENT);
	}

	/**
	 * Get the fallback version of the schema.
	 */
	public static SchemaVersion fallback() {
		return new SchemaVersion(FALLBACK);
	}

	/**
	 * Writes the current schema version as a meta-data file to the given
	 * writer.
	 */
	public void writeTo(JsonStoreWriter writer) {
		writer.put(FILE_NAME, toJson());
	}

	public static SchemaVersion of(JsonStoreReader reader) {
		return of(reader.getJson(FILE_NAME));
	}

	public static SchemaVersion of(JsonElement json) {
		var value = json != null && json.isJsonObject()
				? Json.getInt(json.getAsJsonObject(), "version", FALLBACK)
				: FALLBACK;
		return new SchemaVersion(value);
	}

	public boolean isCurrent() {
		return value == CURRENT;
	}

	public boolean isOlder() {
		return value < CURRENT;
	}

	public boolean isNewer() {
		return value > CURRENT;
	}

	public JsonObject toJson() {
		var json = new JsonObject();
		json.addProperty("version", value);
		return json;
	}

}
