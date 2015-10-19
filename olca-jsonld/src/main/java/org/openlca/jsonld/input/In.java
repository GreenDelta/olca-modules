package org.openlca.jsonld.input;

import java.util.Date;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Dates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class In {

	private In() {
	}

	static String getString(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
	}

	static double getDouble(JsonObject obj, String property,
			double defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		else
			return elem.getAsDouble();
	}

	static boolean getBool(JsonObject obj, String property,
			boolean defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		else
			return elem.getAsBoolean();
	}

	static Date getDate(JsonObject obj, String property) {
		String xmlString = getString(obj, property);
		return Dates.fromString(xmlString);
	}

	/**
	 * Returns the ID of a referenced entity (see Out.writeRef).
	 */
	static String getRefId(JsonObject obj, String refName) {
		JsonObject ref = getObject(obj, refName);
		if (ref == null)
			return null;
		return getString(ref, "@id");
	}

	static JsonObject getObject(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonObject())
			return null;
		else
			return elem.getAsJsonObject();
	}

	static JsonArray getArray(JsonObject obj, String property) {
		if (obj == null || property == null)
			return null;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonArray())
			return null;
		else
			return elem.getAsJsonArray();
	}

	static long getVersion(JsonObject obj) {
		if (obj == null)
			return 0;
		String version = getString(obj, "version");
		if (version != null)
			return Version.fromString(version).getValue();
		else
			return 0;
	}

	static long getLastChange(JsonObject obj) {
		if (obj == null)
			return 0;
		String lastChange = getString(obj, "lastChange");
		if (lastChange != null)
			return Dates.getTime(lastChange);
		else
			return 0;
	}

	static void mapAtts(JsonObject obj, RootEntity entity) {
		if (obj == null || entity == null)
			return;
		entity.setName(getString(obj, "name"));
		entity.setDescription(getString(obj, "description"));
		entity.setRefId(getString(obj, "@id"));
		entity.setVersion(getVersion(obj));
		entity.setLastChange(getLastChange(obj));
	}
}
