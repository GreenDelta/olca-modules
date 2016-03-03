package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.Currency;
import org.openlca.core.model.descriptors.CurrencyDescriptor;

public class CurrencyDao
		extends CategorizedEntityDao<Currency, CurrencyDescriptor> {

	public CurrencyDao(IDatabase db) {
		super(Currency.class, CurrencyDescriptor.class, db);
	}

	/**
	 * There should be only one currency that is the reference currency in the
	 * database. This function returns the reference currency of the first
	 * currency in the database (which should by the reference currency of all
	 * other currencies by this definition). If there is no currency contained
	 * in the database, this function will return null.
	 */
	public Currency getReferenceCurrency() {
		List<CurrencyDescriptor> all = getDescriptors();
		if (all.isEmpty())
			return null;
		Currency currency = getForId(all.get(0).getId());
		return currency == null ? null : currency.referenceCurrency;
	}
}
