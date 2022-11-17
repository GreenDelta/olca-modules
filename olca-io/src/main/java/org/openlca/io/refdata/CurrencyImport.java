package org.openlca.io.refdata;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class CurrencyImport implements Runnable {

	private final ImportConfig config;

	CurrencyImport(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {

		var currencies = new ArrayList<Currency>();
		var ref = new AtomicReference<String>();
		config.eachRowOf("currencies.csv", row -> {
			var c = new Currency();
			c.refId = row.get(0);
			c.name = row.get(1);
			c.description = row.get(2);
			c.category = config.category(ModelType.CURRENCY, row.get(3));
			c.code = row.get(5);
			c.conversionFactor  = row.getDouble(6);
			if (ref.get() == null) {
				ref.set(row.get(4));
			}
			currencies.add(c);
		});

		var refId = ref.get();
		if (Strings.nullOrEmpty(refId)) {
			config.log().error("no reference currency defined");
			return;
		}
		var refCurrency = currencies.stream()
				.filter(c -> Objects.equals(
						refId, c.refId) || Objects.equals(refId, c.name))
				.findAny()
				.orElse(null);
		if (refCurrency == null) {
			config.log().error("unknown reference currency: " + refId);
			return;
		}
		config.insert(refCurrency);

		var others = currencies.stream()
				.filter(c -> !refCurrency.equals(c))
				.peek(c -> c.referenceCurrency = refCurrency)
				.toList();
		config.insert(others);
	}
}
