package org.openlca.core.database.descriptors;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.CurrencyDescriptor;

public class CurrencyDescriptors
		extends RootDescriptorReader<CurrencyDescriptor> {

	private CurrencyDescriptors(IDatabase db) {
		super(new CurrencyDao(db));
	}

	public static CurrencyDescriptors of(IDatabase db) {
		return new CurrencyDescriptors(db);
	}
}
