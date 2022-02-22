package org.openlca.jsonld.output;

import org.openlca.core.model.Currency;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class CurrencyWriter extends Writer<Currency> {

	CurrencyWriter(JsonExport export) {
		super(export);
	}

	@Override
	JsonObject write(Currency c) {
		JsonObject obj = super.write(c);
		if (obj == null)
			return null;
		Json.put(obj, "code", c.code);
		Json.put(obj, "conversionFactor", c.conversionFactor);
		Json.put(obj, "referenceCurrency", exp.handleRef(c.referenceCurrency));
		return obj;
	}
}
