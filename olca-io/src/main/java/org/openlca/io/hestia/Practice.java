package org.openlca.io.hestia;

import com.google.gson.JsonObject;

public record Practice(JsonObject json) {

	public Term term() {
		var obj = json.getAsJsonObject("term");
		return obj != null
			? new Term(obj)
			: null;
	}

	public double value() {
		return Util.firstValueOf(json);
	}
}
