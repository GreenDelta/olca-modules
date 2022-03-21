package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Currency;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

public record CurrencyReader(EntityResolver resolver)
	implements EntityReader<Currency> {

	public CurrencyReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Currency read(JsonObject json) {
		var currency = new Currency();
		update(currency, json);
		return currency;
	}

	@Override
	public void update(Currency currency, JsonObject json) {
		Util.mapBase(currency, json, resolver);
		currency.code = Json.getString(json, "code");
		currency.conversionFactor = Json.getDouble(json, "conversionFactor", 1.0);
		var refCurrencyId = Json.getRefId(json, "refCurrency");
		if (Strings.notEmpty(refCurrencyId)) {
			if (Objects.equals(refCurrencyId, currency.refId)) {
				currency.referenceCurrency = currency;
			} else {
				currency.referenceCurrency = resolver.get(
					Currency.class, refCurrencyId);
			}
		}
	}
}
