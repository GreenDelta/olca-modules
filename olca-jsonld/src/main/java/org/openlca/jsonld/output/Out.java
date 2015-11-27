package org.openlca.jsonld.output;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Dates;
import org.openlca.jsonld.Enums;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class Out {

	static final boolean WRITE_NULL_VALUES = false;
	static final boolean WRITE_EMPTY_COLLECTIONS = false;

	private Out() {
	}

	static void put(JsonObject json, String property, RootEntity value,
			ExportConfig conf) {
		put(json, property, value, conf, false);
	}

	static void put(JsonObject json, String property, RootEntity value,
			ExportConfig conf, boolean forceExport) {
		if (!isValidInput(value))
			return;
		JsonObject ref = References.create(value, conf, forceExport);
		json.add(property, ref);
	}

	static void put(JsonObject json, String property,
			List<? extends RootEntity> values, ExportConfig conf) {
		put(json, property, values, conf, false);
	}

	static void put(JsonObject json, String property,
			List<? extends RootEntity> values, ExportConfig conf,
			boolean forceExport) {
		if (!isValidInput(values))
			return;
		JsonArray array = new JsonArray();
		for (RootEntity value : values)
			array.add(References.create(value, conf, forceExport));
		json.add(property, array);
	}

	static <T extends Enum<T>> void put(JsonObject json, String property,
			Enum<T> value) {
		if (!isValidInput(value))
			return;
		json.addProperty(property, Enums.getLabel(value));
	}

	static void put(JsonObject json, String property, JsonElement value) {
		if (!isValidInput(value))
			return;
		json.add(property, value);
	}

	static void put(JsonObject json, String property, String value) {
		if (!isValidInput(value))
			return;
		json.addProperty(property, value);
	}

	static void put(JsonObject json, String property, Number value) {
		if (!isValidInput(value))
			return;
		json.addProperty(property, value);
	}

	static void put(JsonObject json, String property, Boolean value) {
		if (!isValidInput(value))
			return;
		json.addProperty(property, value);
	}

	static void put(JsonObject json, String property, Character value) {
		if (!isValidInput(value))
			return;
		json.addProperty(property, value);
	}

	static void put(JsonObject json, String property, Date value) {
		if (!isValidInput(value))
			return;
		json.addProperty(property, Dates.toString(value));
	}

	private static boolean isValidInput(Object value) {
		if (Collection.class.isInstance(value))
			return WRITE_EMPTY_COLLECTIONS
					|| !Collection.class.cast(value).isEmpty();
		if (JsonArray.class.isInstance(value))
			return WRITE_EMPTY_COLLECTIONS
					|| JsonArray.class.cast(value).size() > 0;
		return WRITE_NULL_VALUES || value != null;
	}

}
