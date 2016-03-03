package org.openlca.core.database.usage;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CurrencyDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

public class CurrencyUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<CurrencyDescriptor> search;

	@Before
	public void setup() {
		search = IUseSearch.FACTORY.createFor(ModelType.CURRENCY, database);
	}

	@Test
	public void testFindNoUsage() {
		Currency currency = createCurrency();
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(currency));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
		database.createDao(Currency.class).delete(currency);
	}

	@Test
	public void testFindInCurrency() {
		Currency currency = createCurrency();
		Currency other = createCurrency();
		other.referenceCurrency = currency;
		database.createDao(Currency.class).update(other);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(currency));
		database.createDao(Currency.class).delete(currency);
		database.createDao(Currency.class).delete(other);
		BaseDescriptor expected = Descriptors.toDescriptor(other);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	@Test
	public void testFindInExchanges() {
		Currency currency = createCurrency();
		Process process = createProcess(currency, true);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(currency));
		database.createDao(Process.class).delete(process);
		database.createDao(Currency.class).delete(currency);
		BaseDescriptor expected = Descriptors.toDescriptor(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	@Test
	public void testFindInProcess() {
		Currency currency = createCurrency();
		Process process = createProcess(currency, false);
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(currency));
		database.createDao(Process.class).delete(process);
		database.createDao(Currency.class).delete(currency);
		BaseDescriptor expected = Descriptors.toDescriptor(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Currency createCurrency() {
		Currency currency = new Currency();
		currency.setName("currency");
		database.createDao(Currency.class).insert(currency);
		return currency;
	}

	private Process createProcess(Currency currency, boolean inExchange) {
		Process process = new Process();
		process.setName("process");
		if (inExchange) {
			Exchange exchange = new Exchange();
			exchange.currency = currency;
			process.getExchanges().add(exchange);
		} else 
			process.currency = currency;
		database.createDao(Process.class).insert(process);
		return process;
	}
}
