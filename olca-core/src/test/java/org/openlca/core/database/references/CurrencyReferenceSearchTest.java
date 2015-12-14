package org.openlca.core.database.references;

import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.ModelType;

public class CurrencyReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.CURRENCY;
	}

	@Override
	protected Currency createModel() {
		Currency currency = new Currency();
		currency.setCategory(addExpected(new Category()));
		currency.referenceCurrency = addExpected(new Currency());
		return currency;
	}

}
