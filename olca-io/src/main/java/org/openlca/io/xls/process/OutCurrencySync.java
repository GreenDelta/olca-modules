package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.model.Currency;
import org.openlca.core.model.RootEntity;

class OutCurrencySync implements OutEntitySync {

	private final OutConfig config;
	private final Set<Currency> currencies = new HashSet<>();

	OutCurrencySync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Currency c) {
			currencies.add(c);
			if (c.referenceCurrency != null) {
				currencies.add(c.referenceCurrency);
			}
		}
	}

	@Override
	public void flush() {
		var sheet = config.createSheet(Tab.CURRENCIES)
			.withColumnWidths(5, 25);
		sheet.header(
			Field.UUID,
			Field.NAME,
			Field.CATEGORY,
			Field.DESCRIPTION,
			Field.LAST_CHANGE,
			Field.VERSION,
			Field.CODE,
			Field.IS_REFERENCE,
			Field.CONVERSION_FACTOR
			);
		for (var c : Out.sort(currencies)) {
			sheet.next(row ->
				row.next(c.refId)
				.next(c.name)
				.next(Out.pathOf(c))
				.next(c.description)
				.nextAsDate(c.lastChange)
				.nextAsVersion(c.version)
				.next(c.code)
				.next(Objects.equals(c, c.referenceCurrency) ? "yes" : null)
				.next(c.conversionFactor));
		}
	}
}
