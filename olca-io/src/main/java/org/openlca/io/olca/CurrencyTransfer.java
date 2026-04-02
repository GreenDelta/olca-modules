package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.Currency;

public final class CurrencyTransfer implements EntityTransfer<Currency> {

	private final TransferConfig conf;

	public CurrencyTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var origin : conf.source().getAll(Currency.class)) {
			sync(origin);
		}
	}

	@Override
	public Currency sync(Currency origin) {
		return conf.sync(origin, () -> {
			var copy = origin.copy();
			copy.referenceCurrency = Objects.equals(origin, origin.referenceCurrency)
				? copy
				: conf.swap(origin.referenceCurrency);
			return copy;
		});
	}
}
