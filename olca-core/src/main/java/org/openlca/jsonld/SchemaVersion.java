package org.openlca.jsonld;

import com.google.gson.JsonObject;

public record SchemaVersion(int value) {

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
	 * Writes the current schema version as a meta-data file to the given writer.
	 */
	public void writeTo(JsonStoreWriter writer) {
		var json = new JsonObject();
		json.addProperty("version", value);
		writer.put("olca-schema.json", json);
	}

	public static SchemaVersion of(JsonStoreReader reader) {
		var json = reader.getJson("olca-schema.json");
		var value =  json != null && json.isJsonObject()
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

}
