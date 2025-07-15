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
	}

	record Product(JsonObject json) implements HestiaExchange {

		public boolean isPrimary() {
			return Json.getBool(json, "primary", false);
		}

		public String variety() {
			return Json.getString(json, "variety");
		}
	}
}
