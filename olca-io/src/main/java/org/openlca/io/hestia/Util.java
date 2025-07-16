package org.openlca.io.hestia;

import com.google.gson.JsonObject;

final class Util {

	private Util() {
	}

	static double firstValueOf(JsonObject json) {
		if (json == null)
			return 0;
		var array = json.getAsJsonArray("value");
		if (array == null)
			return 0;
		for (var e : array) {
			if (!e.isJsonPrimitive())
				continue;
			var prim = e.getAsJsonPrimitive();
			if (prim.isNumber())
				return prim.getAsDouble();
		}
		return 0;
	}

}
