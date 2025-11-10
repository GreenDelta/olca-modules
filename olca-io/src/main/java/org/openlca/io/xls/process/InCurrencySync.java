package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;

class InCurrencySync {

	private final InConfig config;

	private InCurrencySync(InConfig config) {
		this.config = config;
	}

	static void sync(InConfig config) {
		new InCurrencySync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.CURRENCIES);
		if (sheet == null)
			return;
		var refRef = new AtomicReference<Currency>();
		var created = new HashSet<Currency>();
		sheet.eachRow(row -> {
			var refId = row.str(Field.UUID);
			var isReference = row.bool(Field.IS_REFERENCE);
			var wasCreated = new AtomicBoolean(false);
			var synced = config.index().sync(Currency.class, refId, () -> {
				wasCreated.set(true);
				var currency = new Currency();
				In.mapBase(row, currency);
				currency.category = row.syncCategory(config.db(), ModelType.CURRENCY);
				currency.code = row.str(Field.CODE);
				currency.conversionFactor = row.num(Field.CONVERSION_FACTOR);
				return currency;
			});
			if (isReference) {
				refRef.set(synced);
			}
			if (wasCreated.get()) {
				created.add(synced);
			}
		});

		// set the reference currency in case of inserts
		if (created.isEmpty())
			return;
		var refCurrency = refRef.get();
		if (refCurrency == null)
			return;
		if (created.contains(refCurrency)) {
			refCurrency.referenceCurrency = refCurrency;
			refCurrency.conversionFactor = 1;
			refCurrency = config.db().update(refCurrency);
			config.index().put(refCurrency);
		}
		for (var nextNew : created) {
			if (refCurrency.equals(nextNew))
				continue;
			nextNew.referenceCurrency = refCurrency;
			config.index().put(config.db().update(nextNew));
		}
	}
}
