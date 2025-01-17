package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/// Contains the module results for a single indicator. When the indicator
/// is an impact assessment category, the fields `method` and `impact` should
/// be used. Otherwise, the field `indicator` should be used.
public record SmartResult(JsonObject json) {

	public SmartResult() {
		this(new JsonObject());
	}

	public SmartResult(JsonObject json) {
		this.json = Objects.requireNonNull(json);
	}

	public static Optional<SmartResult> of(JsonElement e) {
		return e != null && e.isJsonObject()
			? Optional.of(new SmartResult(e.getAsJsonObject()))
			: Optional.empty();
	}

	public static List<SmartResult> allOf(JsonArray array) {
		if (array == null)
			return List.of();
		var results = new ArrayList<SmartResult>(array.size());
		for (var e : array) {
			of(e).ifPresent(results::add);
		}
		return results;
	}

	public static JsonArray toJsonArray(List<SmartResult> results) {
		if (results == null)
			return new JsonArray();
		var array = new JsonArray(results.size());
		for (var r : results) {
			array.add(r.json);
		}
		return array;
	}

	public String method() {
		return Json.getString(json, "lca_method");
	}

	public SmartResult method(String method) {
		Json.put(json, "lca_method", method);
		return this;
	}

	public String impact() {
		return Json.getString(json, "category");
	}

	public SmartResult impact(String indicator) {
		Json.put(json, "category", indicator);
		return this;
	}

	public String indicator() {
		return Json.getString(json, "indicator");
	}

public SmartResult indicator(String indicator) {
		Json.put(json, "indicator", indicator);
		return this;
	}

	public String unit() {
		return Json.getString(json, "unit");
	}

	public SmartResult unit(String unit) {
		Json.put(json, "unit", unit);
		return this;
	}

	public List<SmartModuleValue> values() {
		return SmartModuleValue.get(json);
	}

	public SmartResult values(List<SmartModuleValue> values) {
		SmartModuleValue.set(json, values);
		return this;
	}
}
