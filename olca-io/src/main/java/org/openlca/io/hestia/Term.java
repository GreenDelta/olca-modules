package org.openlca.io.hestia;

import java.util.Locale;

import org.openlca.commons.Strings;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record Term(JsonObject json) implements HestiaObject {

	public String termType() {
		return Json.getString(json, "termType");
	}

	public String name() {
		return Json.getString(json, "name");
	}

	public String unit() {
		return Json.getString(json, "units");
	}

	/// Try to convert the type of this term to a category name. Returns `null`
	/// when this is not possible.
	public String getCategoryName() {
		var type = termType();
		if (Strings.isBlank(type))
			return null;
		var b = new StringBuilder();
		for (int i = 0; i < type.length(); i++) {
			var c = type.charAt(i);
			if (Character.isUpperCase(c)) {
				b.append(' ');
			}
			b.append(c);
		}
		return b.toString().toLowerCase(Locale.US);
	}

}
