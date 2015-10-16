package org.openlca.core.matrix;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Currency;

public class CurrencyTableTest {

	private CurrencyDao dao = new CurrencyDao(Tests.getDb());

	@Test
	public void testGetFactor() {
		Currency eur = make("EUR", 1.0);
		Currency usd = make("USD", 0.88);
		usd.referenceCurrency = eur;
		dao.update(usd);
		CurrencyTable table = CurrencyTable.create(Tests.getDb());
		Assert.assertEquals(1.0, table.getFactor(eur.getId()), 1e-10);
		Assert.assertEquals(0.88, table.getFactor(usd.getId()), 1e-10);
	}

	private Currency make(String code, double factor) {
		Currency c = new Currency();
		c.code = code;
		c.conversionFactor = factor;
		c.referenceCurrency = c;
		return dao.insert(c);
	}

}
