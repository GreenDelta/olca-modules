package org.openlca.io.openepd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class Util {

	static LocalDate getDate(JsonObject obj, String field) {
		var s = Json.getString(obj, field);
		if (s == null)
			return null;
		try {
			return LocalDate.parse(s);
		} catch (Exception e) {
			return null;
		}
	}

	static void put(JsonObject obj, String field, LocalDate date) {
		if (obj == null || field == null || date == null)
			return;
		Json.put(obj, field, date + "T00:00");
	}

	static void put(JsonObject obj, String field, Jsonable v) {
		if (obj == null || field == null || v == null)
			return;
		Json.put(obj, field, v.toJson());
	}

	static EpdQuantity getQuantity(JsonObject obj, String field) {
		if (obj == null || field == null)
			return null;
		return EpdQuantity.fromJson(obj.get(field))
			.orElse(null);
	}
}
