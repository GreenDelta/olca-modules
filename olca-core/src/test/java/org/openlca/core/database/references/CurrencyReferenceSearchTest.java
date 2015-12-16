package org.openlca.core.database.references;

import org.openlca.core.Tests;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;

public class CurrencyReferenceSearchTest extends BaseReferenceSearchTest {

	private Currency currency;

	@Override
	public void clear() {
		new CurrencyDao(Tests.getDb()).delete(currency);
		Tests.clearDb();
	}

	@Override
	protected ModelType getModelType() {
		return ModelType.CURRENCY;
	}

	@Override
	protected Currency createModel() {
		currency = new Currency();
		currency.setCategory(insertAndAddExpected(new Category()));
		currency.referenceCurrency = insertAndAddExpected(new Currency());
		currency = Tests.insert(currency);
		return currency;
	}

}
