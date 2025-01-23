package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record SmartProjectSettings(JsonObject json) {

	public SmartProjectSettings() {
		this(new JsonObject());
	}

	public SmartProjectSettings(JsonObject json) {
		this.json = Objects.requireNonNull(json);
	}

	private static List<String> stringListOf(JsonArray array) {
		if (array == null) {
			return List.of();
		}
		var list = new ArrayList<String>(array.size());
		for (var e : array) {
			if (!e.isJsonPrimitive())
				continue;
			var prim = e.getAsJsonPrimitive();
			if (prim.isString()) {
				list.add(prim.getAsString());
			}
		}
		return list;
	}

	public List<String> resultTables() {
		return stringListOf(Json.getArray(json, "result_tables"));
	}

	public ImpactSettings impactSettings() {
		var obj = Json.getObject(json, "impacts");
		return obj == null
			? new ImpactSettings()
			: new ImpactSettings(obj);
	}


	public record ImpactSettings(JsonObject json) {

		public ImpactSettings() {
			this(new JsonObject());
		}

		public ImpactSettings(JsonObject json) {
			this.json = Objects.requireNonNull(json);
		}

		public List<String> methods() {
			return stringListOf(Json.getArray(json, "available_lcia_methods"));
		}

		public List<String> indicators() {
			return stringListOf(Json.getArray(json, "available_categories"));
		}

		public List<String> units() {
			return stringListOf(Json.getArray(json, "available_units"));
		}

		public List<String> initial() {
			return stringListOf(Json.getArray(json, "initial_settings"));
		}
	}

}
