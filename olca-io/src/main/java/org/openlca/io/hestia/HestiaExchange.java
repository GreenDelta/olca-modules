package org.openlca.io.hestia;

import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public sealed interface HestiaExchange {

	JsonObject json();

	default Term term() {
		var obj = Json.getObject(json(), "term");
		return obj != null
			? new Term(obj)
			: null;
	}

	default double value() {
		return Util.firstValueOf(json());
	}

	record Input(JsonObject json) implements HestiaExchange {
	}

	record Emission(JsonObject json) implements HestiaExchange {

		public String methodModelDescription() {
			return Json.getString(json, "methodModelDescription");
		}

		public MethodTier methodTier() {
			var s = Json.getString(json, "methodTier");
			return s != null
				? MethodTier.fromString(s)
				: null;
		}
	}

	record Product(JsonObject json) implements HestiaExchange {

		public boolean isPrimary() {
			return Json.getBool(json, "primary", false);
		}

		public String variety() {
			return Json.getString(json, "variety");
		}
	}

	record Practice(JsonObject json) implements HestiaExchange {
	}
}
