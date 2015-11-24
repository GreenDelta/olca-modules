package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.Currency;

import com.google.gson.JsonObject;

class CurrencyWriter extends Writer<Currency> {

	CurrencyWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(Currency c) {
		JsonObject obj = super.write(c);
		if (obj == null)
			return null;
		Out.put(obj, "code", c.code);
		Out.put(obj, "conversionFactor", c.conversionFactor);
		boolean exportRef = !Objects.equals(c, c.referenceCurrency);
		Out.put(obj, "referenceCurrency", c.referenceCurrency, conf, exportRef);
		return obj;
	}
}
