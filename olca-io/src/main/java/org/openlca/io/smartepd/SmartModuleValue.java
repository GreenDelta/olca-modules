package org.openlca.io.smartepd;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public record SmartModuleValue(SmartModule module, double value) {

	public SmartModuleValue(SmartModule module, double value) {
		this.module = Objects.requireNonNull(module);
		this.value = value;
	}

	public static List<SmartModuleValue> get(JsonObject obj) {
		if (obj == null)
			return List.of();
		var list = new ArrayList<SmartModuleValue>();
		for (var m : SmartModule.values()) {
			var v = obj.get(m.name());
			if (v == null || !v.isJsonPrimitive())
				continue;
			var prim = v.getAsJsonPrimitive();
			if (!prim.isNumber())
				continue;
			list.add(new SmartModuleValue(m, prim.getAsDouble()));
		}
		return list;
	}

	public static void set(JsonObject obj, List<SmartModuleValue> values) {
		if (obj == null || values == null)
			return;
		// we also clear the module values that are not in the list
		var map = new EnumMap<SmartModule, Double>(SmartModule.class);
		for (var v : values) {
			map.put(v.module, v.value);
		}
		for (var module : SmartModule.values()) {
			var value = map.get(module);
			if (value == null) {
				obj.remove(module.name());
			} else {
				Json.put(obj, module.name(), value);
			}
		}
	}

	public void addTo(JsonObject obj) {
		if (obj == null)
			return;
		Json.put(obj, module.name(), value);
	}

}
