package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ExchangeCostTest extends AbstractZipTest {

	private final ProcessDao processDao = new ProcessDao(Tests.getDb());
	private final CurrencyDao currencyDao = new CurrencyDao(Tests.getDb());

	@Test
	public void testCostAttributes() {
		Currency currency = createCurrency();
		Process process = createProcess(currency);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(process);
		});
		delete(currency, process);
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		checkImport(process);
	}

	private void checkImport(Process originalProc) {
		Process importedProc = processDao.getForRefId(originalProc.refId);
		Exchange original = originalProc.exchanges.get(0);
		Exchange imported = importedProc.exchanges.get(0);
		Assert.assertEquals(original.costs, imported.costs);

	}

	private void delete(Currency currency, Process process) {
		Assert.assertTrue(currencyDao.contains(currency.refId));
		currencyDao.delete(currency);
		Assert.assertFalse(currencyDao.contains(currency.refId));
		Assert.assertTrue(processDao.contains(process.refId));
		processDao.delete(process);
		Assert.assertFalse(processDao.contains(process.refId));
	}

	private Process createProcess(Currency currency) {
		Process process = new Process();
		process.refId = UUID.randomUUID().toString();
		Exchange exchange = new Exchange();
		process.exchanges.add(exchange);
		exchange.currency = currency;
		exchange.costFormula = "21 + 21";
		exchange.costs = 42d;
		process = processDao.insert(process);
		return process;
	}

	private Currency createCurrency() {
		Currency currency = new Currency();
		currency.refId = UUID.randomUUID().toString();
		currency.code = "USD";
		currency.conversionFactor = 1.0;
		currency.referenceCurrency = currency;
		currency = currencyDao.insert(currency);
		return currency;
	}

}
