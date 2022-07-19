package org.openlca.jsonld;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Utility functions for reading and writing Json data.
 */
public class Json {

	private Json() {
	}

	/**
	 * Return the given property as JSON object.
	 */
	public static JsonObject getObject(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonObject())
			return null;
		else
			return elem.getAsJsonObject();
	}

	/**
	 * Return the given property as JSON array.
	 */
	public static JsonArray getArray(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonArray())
			return null;
		else
			return elem.getAsJsonArray();
	}

	public static Stream<JsonElement> stream(JsonArray array) {
		if (array == null)
			return Stream.empty();
		return StreamSupport.stream(array.spliterator(), false);
	}

	public static void forEachObject(
		JsonObject obj, String property, Consumer<JsonObject> fn) {
		var array = getArray(obj, property);
		if (array == null)
			return;
		for (var elem : array) {
			if (elem.isJsonObject()) {
				fn.accept(elem.getAsJsonObject());
			}
		}
	}

	/**
	 * Return the string value of the given property.
	 */
	public static String getString(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
	}

	/**
	 * Return the double value of the given property.
	 */
	public static double getDouble(JsonObject obj,
		String property, double defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		else
			return elem.getAsDouble();
	}

	/**
	 * Return the int value of the given property.
	 */
	public static int getInt(JsonObject obj, String property, int defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		else
			return elem.getAsInt();
	}

	public static OptionalInt getInt(JsonObject obj, String property) {
		if (obj == null || property == null)
			return OptionalInt.empty();
		var elem = obj.get(property);
		try {
			return elem == null || !elem.isJsonPrimitive()
				? OptionalInt.empty()
				: OptionalInt.of(elem.getAsInt());
		} catch (Exception e) {
			return OptionalInt.empty();
		}
	}

	public static long getLong(JsonObject obj, String property, long defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		var elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		var prim = elem.getAsJsonPrimitive();
		return prim.isNumber()
			? prim.getAsLong()
			: defaultVal;
	}

	/**
	 * Writes the given date as ISO 8601 string to the given JSON object.
	 */
	public static void put(JsonObject json, String property, Date date) {
		if (date == null)
			return;
		var str = date.toInstant().toString();
		put(json, property, str);
	}

	public static void put(JsonObject json, String property, Boolean value) {
		if (json == null || property == null || value == null)
			return;
		json.addProperty(property, value);
	}


	public static void put(JsonObject json, String property, Number value) {
		if (json == null || property == null || value == null)
			return;
		json.addProperty(property, value);
	}

	public static void put(JsonObject json, String property, JsonElement elem) {
		if (json == null || property == null || elem == null)
			return;
		json.add(property, elem);
	}

	public static <T extends Enum<T>> void put(
		JsonObject json, String property, Enum<T> value) {
		if (value == null)
			return;
		put(json, property, Enums.getLabel(value));
	}

	public static OptionalDouble getDouble(JsonObject obj, String property) {
		if (obj == null || property == null)
			return OptionalDouble.empty();
		var elem = obj.get(property);
		return elem == null || !elem.isJsonPrimitive()
			? OptionalDouble.empty()
			: OptionalDouble.of(elem.getAsDouble());
	}

	public static boolean getBool(
		JsonObject obj, String property, boolean defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		else
			return elem.getAsBoolean();
	}

	public static Date getDate(JsonObject obj, String property) {
		return parseDate(getString(obj, property));
	}

	public static Date parseDate(String str) {
		if (Strings.nullOrEmpty(str))
			return null;
		try {
			if (str.length() < 18) {
				// try to parse date strings like "2015-05-23"
				if (str.length() > 10) {
					// in older versions may have a zone offset
					// like "2015-05-23+02:00"
					str = str.substring(0, 10);
				}
				var date = LocalDate.parse(str);
				var seconds = date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC);
				return Date.from(Instant.ofEpochSecond(seconds));
			}
			if (str.endsWith("Z")) {
				// assume UTC time input
				var instant = Instant.parse(str);
				return Date.from(instant);
			}
			// assume offset time
			var offset = OffsetDateTime.parse(str);
			return Date.from(offset.toInstant());
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Json.class);
			log.error("failed to parse date / time: " + str, e);
			return null;
		}
	}

	public static <T extends Enum<T>> T getEnum(
		JsonObject obj, String property, Class<T> enumClass) {
		String value = getString(obj, property);
		return Enums.getValue(value, enumClass);
	}

	/**
	 * Returns the value of the `@id` field of the entity reference with the
	 * given name. For example, the given object could be an exchange and the
	 * given reference name could be `flow`, then, this method would return the
	 * reference ID of the flow.
	 */
	public static String getRefId(JsonObject obj, String refName) {
		JsonObject ref = getObject(obj, refName);
		if (ref == null)
			return null;
		return getString(ref, "@id");
	}

	public static void put(JsonObject obj, String prop, String val) {
		if (obj == null || val == null)
			return;
		obj.addProperty(prop, val);
	}

	public static JsonObject asRef(RefEntity e) {
		if (e == null)
			return null;
		var obj = new JsonObject();
		put(obj, "@type", e.getClass().getSimpleName());
		put(obj, "@id", e.refId);
		put(obj, "name", e.name);

		if (e instanceof RootEntity ce
			&& ce.category != null) {
			put(obj, "category", ce.category.toPath());
		}

		// process
		if (e instanceof Process process) {
			put(obj, "processType", Enums.getLabel(process.processType));
			if (process.location != null) {
				put(obj, "location", process.location.code);
			}
		}

		// flows
		if (e instanceof Flow flow) {
			put(obj, "flowType", Enums.getLabel(flow.flowType));
			if (flow.location != null) {
				put(obj, "location", flow.location.code);
			}
			var refUnit = flow.getReferenceUnit();
			if (refUnit != null) {
				put(obj, "refUnit", refUnit.name);
			}
		}

		// flow properties
		if (e instanceof FlowProperty property) {
			var refUnit = property.getReferenceUnit();
			if (refUnit != null) {
				put(obj, "refUnit", refUnit.name);
			}
		}

		return obj;
	}

	/**
	 * This method creates a data set reference for the given descriptor. The
	 * created reference will not contain some useful meta-data like `refUnit`
	 * or `category`.
	 */
	public static JsonObject asRef(Descriptor d) {
		if (d == null)
			return null;
		var obj = new JsonObject();
		if (d.type != null) {
			String type = d.type.getModelClass().getSimpleName();
			put(obj, "@type", type);
		}
		put(obj, "@id", d.refId);
		put(obj, "name", d.name);
		return obj;
	}

	/**
	 * Writes the given JSON element to the given file. Possible exceptions are
	 * rethrown as runtime exceptions.
	 */
	public static void write(JsonElement json, File file) {
		if (json == null)
			return;
		try (var stream = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
				 var buffer = new BufferedWriter(writer)) {
			new Gson().toJson(json, buffer);
		} catch (Exception e) {
			throw new RuntimeException("failed to write JSON file " + file, e);
		}
	}

	public static Optional<JsonElement> read(File file) {
		return read(file, JsonElement.class);
	}

	/**
	 * Read the content of the given file as JSON object. If this fails an empty
	 * result is returned instead of throwing an exception.
	 */
	public static Optional<JsonObject> readObject(File file) {
		return read(file, JsonObject.class);
	}

	public static Optional<JsonObject> readObject(InputStream stream) {
		return read(stream, JsonObject.class);
	}

	public static Optional<JsonArray> readArray(File file) {
		return read(file, JsonArray.class);
	}

	private static <T> Optional<T> read(File file, Class<T> type) {
		if (file == null)
			return Optional.empty();
		try (var stream = new FileInputStream(file)) {
			return read(stream, type);
		} catch (IOException e) {
			var log = LoggerFactory.getLogger(Json.class);
			log.error("failed to read JSON file " + file, e);
			return Optional.empty();
		}
	}

	private static <T> Optional<T> read(InputStream stream, Class<T> type) {
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
				 var buffer = new BufferedReader(reader)) {
			var obj = new Gson().fromJson(buffer, type);
			return Optional.of(obj);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Json.class);
			log.error("failed to read JSON", e);
			return Optional.empty();
		}
	}
}
