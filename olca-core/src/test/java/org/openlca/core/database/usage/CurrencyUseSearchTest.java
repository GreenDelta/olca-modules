package org.openlca.core.database.usage;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;

public class CurrencyUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private final UsageSearch search = UsageSearch.of(ModelType.CURRENCY, db);

	@Test
	public void testFindNoUsage() {
		var currency = db.insert(Currency.of("EUR"));
		var models = search.find(currency.id);
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		db.delete(currency);
	}

	@Test
	public void testFindInCurrency() {
		var currency = db.insert(Currency.of("EUR"));
		var other = db.insert(Currency.of("USD"));
		other.referenceCurrency = currency;
		db.update(other);
		var results = search.find(currency.id);
		db.delete(currency, other);
		var expected = Descriptor.of(other);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.iterator().next());
	}

	@Test
	public void testFindInExchanges() {
		var currency = db.insert(Currency.of("EUR"));
		var process = createProcess(currency);
		var results = search.find(currency.id);
		db.delete(process, currency);
		var expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.iterator().next());
	}

	private Process createProcess(Currency currency) {
		var process = new Process();
		process.name = "process";
		var exchange = new Exchange();
		exchange.currency = currency;
		process.exchanges.add(exchange);
		return db.insert(process);
	}
}
