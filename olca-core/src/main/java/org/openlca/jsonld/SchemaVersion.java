package org.openlca.jsonld;

import com.google.gson.JsonObject;

public final class SchemaVersion {

	/**
	 * The fallback version if the schema of the data cannot be identified.
	 */
	public static final int FALLBACK = 1;

	/**
	 * The current schema version that is written be this API.
	 */
	public static final int CURRENT = 2;

	/**
	 * Writes the current schema version as a meta-data file to the given writer.
	 */
	public static void writeTo(JsonStoreWriter writer) {
		var json = new JsonObject();
		json.addProperty("version", CURRENT);
		writer.put("olca-schema.json", json);
	}

	public static int readFrom(JsonStoreReader reader) {
		var json = reader.getJson("olca-schema.json");
		return json != null && !json.isJsonObject()
			? Json.getInt(json.getAsJsonObject(), "version", FALLBACK)
			: FALLBACK;
	}
}
