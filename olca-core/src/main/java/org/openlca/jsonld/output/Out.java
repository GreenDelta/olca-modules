package org.openlca.jsonld.output;

import java.util.Collection;
import java.util.List;

import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Enums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Out {

	static final int FORCE_EXPORT = 2;
	static final int REQUIRED_FIELD = 4;
	private static final Logger log = LoggerFactory.getLogger(Out.class);
	private static final boolean WRITE_NULL_VALUES = false;
	private static final boolean WRITE_EMPTY_COLLECTIONS = false;

	private Out() {
	}

	private static boolean is(int flags, int expected) {
		return (flags & expected) == expected;
	}

	static void put(JsonObject json, String property, RootEntity value, ExportConfig conf) {
		put(json, property, value, conf, 0);
	}

	static void put(JsonObject json, String property, RootEntity value, ExportConfig conf, int flags) {
		if (!checkValidInput(json, property, value, flags))
			return;
		JsonObject ref = References.create(value, conf, is(flags, FORCE_EXPORT));
		json.add(property, ref);
	}

	static void put(JsonObject json, String property, List<? extends RootEntity> values, ExportConfig conf) {
		put(json, property, values, conf, 0);
	}

	static void put(JsonObject json, String property, List<? extends RootEntity> values, ExportConfig conf, int flags) {
		if (!checkValidInput(json, property, values, flags))
			return;
		JsonArray array = new JsonArray();
		for (RootEntity value : values)
			array.add(References.create(value, conf, is(flags, FORCE_EXPORT)));
		json.add(property, array);
	}

	static <T extends Enum<T>> void put(JsonObject json, String property, Enum<T> value) {
		put(json, property, value, 0);
	}

	static <T extends Enum<T>> void put(JsonObject json, String property, Enum<T> value, int flags) {
		if (!checkValidInput(json, property, value, flags))
			return;
		json.addProperty(property, Enums.getLabel(value));
	}

	static void put(JsonObject json, String property, JsonElement value) {
		put(json, property, value, 0);
	}

	static void put(JsonObject json, String property, JsonElement value, int flags) {
		if (!checkValidInput(json, property, value, flags))
			return;
		json.add(property, value);
	}

	static void put(JsonObject json, String property, String value) {
		put(json, property, value, 0);
	}

	static void put(JsonObject json, String property, String value, int flags) {
		if (!checkValidInput(json, property, value, flags))
			return;
		json.addProperty(property, value);
	}

	static void put(JsonObject json, String property, Number value) {
		put(json, property, value, 0);
	}

	static void put(JsonObject json, String property, Number value, int flags) {
		if (!checkValidInput(json, property, value, flags))
			return;
		json.addProperty(property, value);
	}

	static void put(JsonObject json, String property, Boolean value) {
		put(json, property, value, 0);
	}

	static void put(JsonObject json, String property, Boolean value, int flags) {
		if (!checkValidInput(json, property, value, flags))
			return;
		json.addProperty(property, value);
	}

	static void put(JsonObject json, String property, Character value, int flags) {
		if (!checkValidInput(json, property, value, flags))
			return;
		json.addProperty(property, value);
	}

	private static boolean checkValidInput(JsonObject json, String property, Object value, int flags) {
		if (isValidInput(value))
			return true;
		if (is(flags, REQUIRED_FIELD)) {
			String type = json == null || !json.has("@type")
					? "unknown type"
					: json.get("@type").getAsString();
			String refId = json == null || !json.has("@id")
					? ""
					: json.get("@id").getAsString();
			log.warn("JsonExport: Missing required field '{}' on {} {}",
					property, type, refId);
		}
		return false;
	}

	private static boolean isValidInput(Object value) {
		if (value instanceof Collection)
			return WRITE_EMPTY_COLLECTIONS || !((Collection<?>) value).isEmpty();
		if (value instanceof JsonArray)
			return WRITE_EMPTY_COLLECTIONS || ((JsonArray) value).size() > 0;
		return WRITE_NULL_VALUES || value != null;
	}

}
