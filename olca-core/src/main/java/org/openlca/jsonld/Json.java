package org.openlca.jsonld;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Categories;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.slf4j.LoggerFactory;

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
		return StreamSupport.stream(array.spliterator(),  false);
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

	/**
	 * Writes the given date as ISO 8601 string to the given JSON object.
	 */
	public static void put(JsonObject json, String property, Date date) {
		if (date == null)
			return;
		var str = date.toInstant().toString();
		put(json, property, str);
	}

	public static Optional<Double> getDouble(JsonObject obj, String property) {
		if (obj == null || property == null)
			return Optional.empty();
		JsonElement elem = obj.get(property);
		return elem == null || !elem.isJsonPrimitive()
				? Optional.empty()
				: Optional.of(elem.getAsDouble());
	}

	public static boolean getBool(JsonObject obj,
								  String property, boolean defaultVal) {
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
		if (str == null)
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

	public static <T extends Enum<T>> T getEnum(JsonObject obj,
												String property, Class<T> enumClass) {
		String value = getString(obj, property);
		return Enums.getValue(value, enumClass);
	}

	/**
	 * Returns the value of the `@id` field of the entity reference with the given
	 * name. For example, the given object could be an exchange and the given
	 * reference name could be `flow`, then, this method would return the reference
	 * ID of the flow.
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

	/**
	 * Generates a `Ref` type as defined in olca-schema. For some types (e.g. flows
	 * or processes) a more specific `Ref` type is used (e.g. `FlowRef` or
	 * `ProcessRef`) that contains additional meta-data.
	 */
	public static JsonObject asRef(Descriptor d, EntityCache cache) {
		if (d == null)
			return null;
		JsonObject obj = new JsonObject();
		if (d.type != null) {
			String type = d.type.getModelClass().getSimpleName();
			put(obj, "@type", type);
		}
		put(obj, "@id", d.refId);
		put(obj, "name", d.name);
		if (d instanceof CategorizedDescriptor) {
			putCategoryPath(obj, (CategorizedDescriptor) d, cache);
		}
		if (d instanceof CategoryDescriptor) {
			putCategoryMetaData(obj, (CategoryDescriptor) d);
		}
		if (d instanceof FlowDescriptor) {
			putFlowMetaData(obj, (FlowDescriptor) d, cache);
		}
		if (d instanceof ProcessDescriptor) {
			putProcessMetaData(obj, (ProcessDescriptor) d, cache);
		}
		if (d instanceof ImpactCategoryDescriptor) {
			ImpactCategoryDescriptor icd = (ImpactCategoryDescriptor) d;
			obj.addProperty("refUnit", icd.referenceUnit);
		}
		return obj;
	}

	/**
	 * Generates a `Ref` type as defined in olca-schema. For some types (e.g. flows
	 * or processes) a more specific `Ref` type is used (e.g. `FlowRef` or
	 * `ProcessRef`) that contains additional meta-data.
	 */
	public static JsonObject asDescriptor(Descriptor d, EntityCache cache) {
		if (d == null)
			return null;
		JsonObject obj = asRef(d, cache);
		if (obj == null)
			return null;
		put(obj, "description", d.description);
		put(obj, "version", Version.asString(d.version));
		return obj;
	}

	private static void putCategoryPath(JsonObject ref,
										CategorizedDescriptor d, EntityCache cache) {
		if (ref == null || d == null || cache == null
				|| d.category == null)
			return;
		Category cat = cache.get(Category.class, d.category);
		if (cat == null)
			return;
		List<String> path = Categories.path(cat);
		JsonArray array = new JsonArray();
		for (String p : path) {
			array.add(new JsonPrimitive(p));
		}
		ref.add("categoryPath", array);
	}

	private static void putCategoryMetaData(JsonObject ref,
											CategoryDescriptor d) {
		if (ref == null || d == null)
			return;
		if (d.categoryType != null) {
			String type = d.categoryType.getModelClass().getSimpleName();
			ref.addProperty("categoryType", type);
		}
	}

	private static void putFlowMetaData(JsonObject ref,
										FlowDescriptor d, EntityCache cache) {
		if (ref == null || d == null)
			return;
		if (d.flowType != null) {
			ref.addProperty("flowType", Enums.getLabel(d.flowType));
		}
		if (cache == null)
			return;
		if (d.location != null) {
			Location loc = cache.get(Location.class, d.location);
			if (loc != null) {
				ref.addProperty("location", loc.code);
			}
		}
		FlowProperty prop = cache.get(FlowProperty.class, d.refFlowPropertyId);
		if (prop != null && prop.unitGroup != null) {
			Unit unit = prop.unitGroup.referenceUnit;
			if (unit != null) {
				ref.addProperty("refUnit", unit.name);
			}
		}
	}

	private static void putProcessMetaData(JsonObject ref,
										   ProcessDescriptor d, EntityCache cache) {
		if (ref == null || d == null)
			return;
		if (d.processType != null) {
			ref.addProperty("processType", Enums.getLabel(d.processType));
		}
		if (cache != null && d.location != null) {
			Location loc = cache.get(Location.class, d.location);
			if (loc != null) {
				ref.addProperty("location", loc.code);
			}
		}
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
			 var buffer = new BufferedWriter(writer)
		) {
			new Gson().toJson(json, buffer);
		} catch (Exception e) {
			throw new RuntimeException("failed to write JSON file " + file, e);
		}
	}

	/**
	 * Read the content of the given file as JSON object. If this fails an
	 * empty result is returned instead of throwing an exception.
	 */
	public static Optional<JsonObject> readObject(File file) {
		return read(file, JsonObject.class);
	}

	public static Optional<JsonArray> readArray(File file) {
		return read(file, JsonArray.class);
	}

	private static <T> Optional<T> read(File file, Class<T> type) {
		if (file == null)
			return Optional.empty();
		try (var stream = new FileInputStream(file);
			 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
			 var buffer = new BufferedReader(reader)) {
			var obj = new Gson().fromJson(buffer, type);
			return Optional.of(obj);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Json.class);
			log.error("failed to read JSON file " + file, e);
			return Optional.empty();
		}
	}
}
