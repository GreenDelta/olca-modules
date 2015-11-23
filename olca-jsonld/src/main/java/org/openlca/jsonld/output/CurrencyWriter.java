package org.openlca.jsonld.output;

import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.model.Currency;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class CurrencyWriter extends Writer<Currency> {

	@Override
	JsonObject write(Currency currency, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(currency, refFn);
		if (obj == null)
			return null;
		Out.put(obj, "code", currency.code);
		Out.put(obj, "conversionFactor", currency.conversionFactor);
		if (Objects.equals(currency, currency.referenceCurrency))
			Out.put(obj, "referenceCurrency", currency.referenceCurrency, null);
		else
			Out.put(obj, "referenceCurrency", currency.referenceCurrency, refFn);
		return obj;
	}
}
