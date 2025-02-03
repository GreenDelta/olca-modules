package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record SmartResultList(
	SmartIndicatorType type, JsonObject json
) {

	public SmartResultList(SmartIndicatorType type) {
		this(type, new JsonObject());
	}

	public SmartResultList(SmartIndicatorType type, JsonObject json) {
		this.type = Objects.requireNonNull(type);
		this.json = Objects.requireNonNull(json);
	}

	public static Optional<SmartResultList> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		for (var type : SmartIndicatorType.values()) {
			if (obj.has(keyOf(type)))
				return Optional.of(new SmartResultList(type, obj));
		}
		return Optional.empty();
	}

	private static String keyOf(SmartIndicatorType type) {
		if (type == null)
			return null;
		return switch (type) {
			case IMPACT -> "impacts";
			case RESOURCE -> "resource_uses";
			case OUTPUT -> "output_flows";
		};
	}

	public static List<SmartResultList> allOf(JsonArray array) {
		if (array == null)
			return List.of();
		var results = new ArrayList<SmartResultList>(array.size());
		for (var e : array) {
			of(e).ifPresent(results::add);
		}
		return results;
	}

	public String header() {
		return Json.getString(json, "header");
	}

	public SmartResultList header(String header) {
		Json.put(json, "header", header);
		return this;
	}

	public List<SmartResult> results() {
		return SmartResult.allOf(Json.getArray(json, keyOf(type)));
	}

	public SmartResultList results(List<SmartResult> impacts) {
		Json.put(json, keyOf(type), SmartResult.toJsonArray(impacts));
		return this;
	}
}
