package org.openlca.core.database;

import org.openlca.core.model.Currency;
import org.openlca.core.model.descriptors.CurrencyDescriptor;

public class CurrencyDao
		extends CategorizedEntityDao<Currency, CurrencyDescriptor> {

	public CurrencyDao(IDatabase db) {
		super(Currency.class, CurrencyDescriptor.class, db);
	}

}
