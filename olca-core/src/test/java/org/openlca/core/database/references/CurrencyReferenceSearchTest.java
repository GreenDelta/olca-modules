package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.Tests;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;

public class CurrencyReferenceSearchTest extends BaseReferenceSearchTest {

	private final List<Currency> currencies = new ArrayList<>();

	@Override
	public void clear() {
		for (Currency currency : currencies)
			new CurrencyDao(Tests.getDb()).delete(currency);
		db.clear();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.CURRENCY;
	}

	@Override
	protected Currency createModel() {
		Currency currency = new Currency();
		currency.category = insertAndAddExpected("category", new Category());
		currency.referenceCurrency = insertAndAddExpected("referenceCurrency",
				new Currency());
		currency = db.insert(currency);
		currencies.add(currency);
		return currency;
	}

}
