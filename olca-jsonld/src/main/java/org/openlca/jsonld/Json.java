package org.openlca.jsonld;

import java.util.Date;

import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Utility functions for reading and writing Json data.
 */
public class Json {

	private Json() {
	}

	/** Return the given property as JSON object. */
	public static JsonObject getObject(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonObject())
			return null;
		else
			return elem.getAsJsonObject();
	}

	/** Return the given property as JSON array. */
	public static JsonArray getArray(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonArray())
			return null;
		else
			return elem.getAsJsonArray();
	}

	/** Return the string value of the given property. */
	public static String getString(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
	}

	/** Return the double value of the given property. */
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

	/** Return the int value of the given property. */
	public static int getInt(JsonObject obj,
			String property, int defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		else
			return elem.getAsInt();
	}

	public static Double getOptionalDouble(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsDouble();
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
		String xmlString = getString(obj, property);
		return Dates.fromString(xmlString);
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

	public static JsonObject toJson(BaseDescriptor d) {
		if (d == null)
			return null;
		JsonObject obj = new JsonObject();
		if (d.getModelType() != null) {
			String type = d.getModelType().getModelClass().getSimpleName();
			put(obj, "@type", type);
		}
		put(obj, "@id", d.getRefId());
		put(obj, "name", d.getName());
		put(obj, "description", d.getDescription());
		return obj;
	}

}
