package org.openlca.jsonld.output;

import org.openlca.core.model.Currency;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record CurrencyWriter(JsonExport exp) implements Writer<Currency> {

	@Override
	public JsonObject write(Currency c) {
		var obj = Writer.init(c);
		Json.put(obj, "code", c.code);
		Json.put(obj, "conversionFactor", c.conversionFactor);
		Json.put(obj, "refCurrency", exp.handleRef(c.referenceCurrency));
		return obj;
	}
}
