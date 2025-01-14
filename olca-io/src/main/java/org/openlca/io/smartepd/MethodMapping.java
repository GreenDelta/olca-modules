package org.openlca.io.smartepd;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openlca.jsonld.Json;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/// Describes a mapping between a SmartEPD and an openLCA LCIA method.
///
/// @param method     the SmartEPD method
/// @param ref        the reference of the openLCA method
/// @param indicators the list of indicators IDs that are defined in the
///                                     openLCA method
record MethodMapping(
		SmartMethod method, Ref ref, Set<String> indicators
) {

	static List<MethodMapping> getDefault() {
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

	private static Optional<MethodMapping> of(JsonElement e) {
		if (e == null || !e.isJsonObject())
			return Optional.empty();
		var obj = e.getAsJsonObject();
		var smartId = Json.getString(obj, "SmartEPD");
		var method = SmartMethod.of(smartId).orElse(null);
		if (method == null)
			return Optional.empty();
		var ref = Ref.of(obj.get("openLCA")).orElse(null);
		if (ref == null)
			return Optional.empty();

		var indicators = new HashSet<String>();
		var array = Json.getArray(obj, "indicators");
		if (array != null) {
			for (var el : array) {
				if (!el.isJsonPrimitive())
					continue;
				var prim = el.getAsJsonPrimitive();
				if (prim.isString()) {
					indicators.add(el.getAsString());
				}
			}
		}

		return Optional.of(new MethodMapping(method, ref, indicators));
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


	JsonObject json() {
		var obj = new JsonObject();
		if (method != null) {
			Json.put(obj, "SmartEPD", method.id());
		}
		if (ref != null) {
			Json.put(obj, "openLCA", ref.json());
		}
		if (indicators != null && !indicators.isEmpty()) {
			var array = new JsonArray(indicators.size());
			for (var ind : indicators) {
				array.add(ind);
			}
			Json.put(obj, "indicators", array);
		}
		return obj;
	}

}
