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
		obj.addProperty("code", currency.code);
		obj.addProperty("conversionFactor", currency.conversionFactor);
		JsonObject ref = null;
		if (Objects.equals(currency, currency.referenceCurrency))
			ref = References.create(currency);
		else
			ref = References.create(currency.referenceCurrency, refFn);
		obj.add("referenceCurrency", ref);
		return obj;
	}
}
