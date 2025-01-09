package org.openlca.io.smartepd;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openlca.jsonld.Json;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

record MethodMapping(
	String smartEpd, Ref ref, List<IndicatorMapping> indicators
) {

	private static Optional<MethodMapping> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		return Optional.of(new MethodMapping(
			Json.getString(obj, "SmartEPD"),
			Ref.of(obj.get("openLCA")).orElse(null),
			IndicatorMapping.allOf(Json.getArray(obj, "indicators"))));
	}

	private static List<MethodMapping> allOf(JsonArray array) {
		if (array == null)
			return List.of();
		var results = new ArrayList<MethodMapping>(array.size());
		for (var el : array) {
			of(el).ifPresent(results::add);
		}
		return results;
	}

	static List<MethodMapping> getAll() {
		var in = MethodMapping.class.getResourceAsStream("method-mappings.json");
		if (in == null)
			return List.of();
		try (in; var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			var array = new Gson().fromJson(reader, JsonArray.class);
			return allOf(array);
		} catch (Exception e) {
			LoggerFactory.getLogger(MethodMapping.class)
				.error("failed to load method mapping", e);
			return List.of();
		}
	}

	JsonObject json() {
		var obj = new JsonObject();
		Json.put(obj, "SmartEPD", smartEpd);
		Json.put(obj, "openLCA", ref != null ? ref.json() : null);
		var array = new JsonArray(indicators.size());
		for (var im : indicators) {
			array.add(im.json());
		}
		Json.put(obj, "indicators", array);
		return obj;
	}

	record IndicatorMapping(String smartEpd, Ref ref) {

		private static Optional<IndicatorMapping> of(JsonElement e) {
			if (e == null || !e.isJsonObject())
				return Optional.empty();
			var obj = e.getAsJsonObject();
			return Optional.of(new IndicatorMapping(
				Json.getString(obj, "SmartEPD"),
				Ref.of(obj.get("openLCA")).orElse(null)));
		}

		private static List<IndicatorMapping> allOf(JsonArray array) {
			if (array == null)
				return List.of();
			var results = new ArrayList<IndicatorMapping>(array.size());
			for (var el : array) {
				of(el).ifPresent(results::add);
			}
			return results;
		}

		JsonObject json() {
			var obj = new JsonObject();
			Json.put(obj, "SmartEPD", smartEpd);
			Json.put(obj, "openLCA", ref != null ? ref.json() : null);
			return obj;
		}

	}

	record Ref(String id, String name) {

		private static Optional<Ref> of(JsonElement e) {
			if (e == null || !e.isJsonObject())
				return Optional.empty();
			var obj = e.getAsJsonObject();
			return Optional.of(new Ref(
				Json.getString(obj, "@id"),
				Json.getString(obj, "name")));
		}

		JsonObject json() {
			var obj = new JsonObject();
			Json.put(obj, "@id", id);
			Json.put(obj, "name", name);
			return obj;
		}
	}
}
