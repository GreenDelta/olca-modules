package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.Currency;

final class CurrencyTransfer implements EntityTransfer<Currency> {

	private final TransferContext ctx;

	CurrencyTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var origin : ctx.source().getAll(Currency.class)) {
			sync(origin);
		}
	}

	@Override
	public Currency sync(Currency origin) {
		return ctx.sync(origin, () -> {
			var copy = origin.copy();
			copy.referenceCurrency = Objects.equals(origin, origin.referenceCurrency)
				? copy
				: ctx.resolve(origin.referenceCurrency);
			return copy;
		});
	}
}
