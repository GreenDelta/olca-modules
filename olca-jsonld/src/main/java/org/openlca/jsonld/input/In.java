package org.openlca.jsonld.input;

import java.util.Date;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.RootEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	static double getDouble(JsonObject obj, String property, double defaultVal) {
		if (obj == null || property == null)
			return defaultVal;
		JsonElement elem = obj.get(property);
		if (elem == null || !elem.isJsonPrimitive())
			return defaultVal;
		else
			return elem.getAsDouble();
	}

	static boolean getBool(JsonObject obj, String property, boolean defaultVal) {
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
		if(xmlString == null)
			return null;
		try {
			XMLGregorianCalendar xml = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(xmlString);
			return xml.toGregorianCalendar().getTime();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(In.class);
			log.error("failed to read date " + xmlString, e);
			return null;
		}
	}

	/**
	 * Returns the ID of a referenced entity (see Out.writeRef).
	 */
	static String getRefId(JsonObject obj, String refName) {
		if (obj == null || refName == null)
			return null;
		JsonElement elem = obj.get(refName);
		if (elem == null || !elem.isJsonObject())
			return null;
		return getString(elem.getAsJsonObject(), "@id");
	}

	static void mapAtts(JsonObject obj, RootEntity entity) {
		if (obj == null || entity == null)
			return;
		entity.setName(getString(obj, "name"));
		entity.setDescription(getString(obj, "description"));
		entity.setRefId(getString(obj, "@id"));
	}

}
