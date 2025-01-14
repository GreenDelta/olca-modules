package org.openlca.io.smartepd;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

record IndicatorMapping(SmartIndicator indicator,	List<Ref> refs) {

	static List<IndicatorMapping> getDefault() {
		var in = IndicatorMapping.class.getResourceAsStream(
				"indicator-mappings.json");
		if (in == null)
			return List.of();
		try (in) {
			var json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			var array = new Gson().fromJson(json, JsonArray.class);
			return allOf(array);
		} catch (Exception e) {
			return List.of();
		}
	}

	private static Optional<IndicatorMapping> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		var smartId = Json.getString(obj, "SmartEPD");
		var indicator = SmartIndicator.of(smartId).orElse(null);
		if (indicator == null)
			return Optional.empty();
		var refs = Ref.allOf(Json.getArray(obj, "openLCA"));
		return Optional.of(new IndicatorMapping(indicator, refs));
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
		if (indicator != null) {
			Json.put(obj, "SmartEPD", indicator.id());
			var type = indicator.type();
			if (type != null) {
				Json.put(obj, "type", type.name().toLowerCase());
			}
		}
		if (refs != null && !refs.isEmpty()) {
			var array = new JsonArray(refs.size());
			for (var ref : refs) {
				array.add(ref.json());
			}
			Json.put(obj, "openLCA", array);
		}
		return obj;
	}
}
