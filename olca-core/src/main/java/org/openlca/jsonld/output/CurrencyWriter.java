package org.openlca.jsonld.output;

import org.openlca.core.model.Currency;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

public record CurrencyWriter(JsonExport exp) implements JsonWriter<Currency> {

	@Override
	public JsonObject write(Currency c) {
		var obj = Util.init(c);
		Json.put(obj, "code", c.code);
		Json.put(obj, "conversionFactor", c.conversionFactor);
		Json.put(obj, "refCurrency", exp.handleRef(c.referenceCurrency));
		return obj;
	}
}
