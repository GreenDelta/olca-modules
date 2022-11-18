package org.openlca.io.refdata;

import java.util.ArrayList;

import org.openlca.core.model.Currency;

class CurrencyExport implements Runnable {

	private final ExportConfig config;

	CurrencyExport(ExportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var currencies = config.db().getAll(Currency.class);
		if (currencies.isEmpty())
			return;
		config.sort(currencies);
		var buffer = new ArrayList<>(7);

		config.writeTo("currencies.csv", csv -> {

			// write column headers
			csv.printRecord(
					"ID",
					"Name",
					"Description",
					"Category",
					"Reference currency",
					"Currency code",
					"Conversion factor");

			for (var currency : currencies) {
				buffer.add(currency.refId);
				buffer.add(currency.name);
				buffer.add(currency.description);
				buffer.add(config.toPath(currency.category));

				var refCurr = currency.referenceCurrency != null
						? currency.referenceCurrency.name
						: "";
				buffer.add(refCurr);

				buffer.add(currency.code);
				buffer.add(currency.conversionFactor);

				csv.printRecord(buffer);
				buffer.clear();
			}
		});
	}

}
