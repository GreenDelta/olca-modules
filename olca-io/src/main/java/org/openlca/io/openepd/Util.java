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

	static Optional<String[]> categoryOf(EpdDoc epd) {
		if (epd == null || epd.productClasses.isEmpty())
			return Optional.empty();
		String path = null;
		for (var c : epd.productClasses) {
			if(Objects.equals(c.first, "io.cqd.ec3")) {
				path = c.second;
				break;
			}
			if (path == null) {
				path = c.second;
			}
		}
		if (Strings.nullOrEmpty(path))
			return Optional.empty();


		var segments = new ArrayList<String>();
		var word = new StringBuilder();
		Runnable nextWord = () -> {
			if (word.length() == 0)
				return;
			segments.add(word.toString());
			word.setLength(0);
		};

		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			switch (c) {
				case '/', '\\', '>', '<' -> nextWord.run();
				default -> word.append(c);
			}
		}
		nextWord.run();


		return segments.size() > 0
			? Optional.of(segments.toArray(String[]::new))
			: Optional.empty();
	}
}
