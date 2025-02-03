package org.openlca.io.smartepd;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartIndicatorMapping(
		SmartIndicator indicator, List<SmartRef> refs
) {

	public SmartIndicatorMapping(SmartIndicator indicator, List<SmartRef> refs) {
		this.indicator = Objects.requireNonNull(indicator);
		this.refs = Objects.requireNonNull(refs);
	}

	public static List<SmartIndicatorMapping> getDefault() {
		var in = SmartIndicatorMapping.class.getResourceAsStream(
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

	private static Optional<SmartIndicatorMapping> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		var smartId = Json.getString(obj, "SmartEPD");
		var indicator = SmartIndicator.of(smartId).orElse(null);
		if (indicator == null)
			return Optional.empty();
		var refs = SmartRef.allOf(Json.getArray(obj, "openLCA"));
		return Optional.of(new SmartIndicatorMapping(indicator, refs));
	}

	private static List<SmartIndicatorMapping> allOf(JsonArray array) {
		if (array == null)
			return List.of();
		var results = new ArrayList<SmartIndicatorMapping>(array.size());
		for (var el : array) {
			of(el).ifPresent(results::add);
		}
		return results;
	}

	JsonObject json() {
		var obj = new JsonObject();
		Json.put(obj, "SmartEPD", indicator.id());
		var type = indicator.type();
		if (type != null) {
			Json.put(obj, "type", type.name().toLowerCase());
		}
		if (!refs.isEmpty()) {
			var array = new JsonArray(refs.size());
			for (var ref : refs) {
				array.add(ref.json());
			}
			Json.put(obj, "openLCA", array);
		}
		return obj;
	}
}
