package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.model.Currency;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class CurrencyTest extends AbstractZipTest {

	@Test
	public void testCurrency() {
		CurrencyDao dao = new CurrencyDao(Tests.getDb());
		Currency currency = createModel(dao);
		doExport(currency, dao);
		doImport(dao, currency);
		dao.delete(currency);
	}

	private Currency createModel(CurrencyDao dao) {
		Currency currency = new Currency();
		currency.name = "currency";
		currency.refId = UUID.randomUUID().toString();
		dao.insert(currency);
		return currency;
	}

	private void doExport(Currency currency, CurrencyDao dao) {
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(currency);
		});
		dao.delete(currency);
		Assert.assertFalse(dao.contains(currency.refId));
	}

	private void doImport(CurrencyDao dao, Currency currency) {
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		Assert.assertTrue(dao.contains(currency.refId));
		Currency clone = dao.getForRefId(currency.refId);
		Assert.assertEquals(currency.name, clone.name);
	}
}
