package org.openlca.proto.io.input;

import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Currency;
import org.openlca.proto.ProtoCurrency;

public record CurrencyReader(EntityResolver resolver)
	implements EntityReader<Currency, ProtoCurrency> {

	@Override
	public Currency read(ProtoCurrency proto) {
		var currency = new Currency();
		update(currency, proto);
		return currency;
	}

	@Override
	public void update(Currency currency, ProtoCurrency proto) {
		Util.mapBase(currency, ProtoBox.of(proto), resolver);
		currency.code = proto.getCode();
		currency.conversionFactor = proto.getConversionFactor();
		var refCurrencyId = proto.getRefCurrency().getId();
		if (Strings.isNotBlank(refCurrencyId)) {
			if (Objects.equals(refCurrencyId, currency.refId)) {
				currency.referenceCurrency = currency;
			} else {
				currency.referenceCurrency = resolver.get(
					Currency.class, refCurrencyId);
			}
		}
	}

}
