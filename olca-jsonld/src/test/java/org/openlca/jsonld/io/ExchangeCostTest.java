package org.openlca.jsonld.io;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.database.CostCategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.CostCategory;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.jsonld.AbstractZipTest;
import org.openlca.jsonld.Tests;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class ExchangeCostTest extends AbstractZipTest {

	private ProcessDao processDao = new ProcessDao(Tests.getDb());
	private CurrencyDao currencyDao = new CurrencyDao(Tests.getDb());
	private CostCategoryDao costCategoryDao = new CostCategoryDao(Tests.getDb());

	@Test
	public void testCostAttributes() {
		Currency currency = createCurrency();
		CostCategory category = createCategory();
		Process process = createProcess(currency, category);
		with(zip -> {
			JsonExport export = new JsonExport(Tests.getDb(), zip);
			export.write(process);
		});
		delete(currency, category, process);
		with(zip -> {
			JsonImport jImport = new JsonImport(zip, Tests.getDb());
			jImport.run();
		});
		checkImport(process);
	}

	private void checkImport(Process originalProc) {
		Process importedProc = processDao.getForRefId(originalProc.getRefId());
		Exchange original = originalProc.getExchanges().get(0);
		Exchange imported = importedProc.getExchanges().get(0);
		Assert.assertEquals(original.costValue, imported.costValue);

	}

	private void delete(Currency currency, CostCategory category, Process process) {
		Assert.assertTrue(currencyDao.contains(currency.getRefId()));
		currencyDao.delete(currency);
		Assert.assertFalse(currencyDao.contains(currency.getRefId()));
		Assert.assertTrue(costCategoryDao.contains(category.getRefId()));
		costCategoryDao.delete(category);
		Assert.assertFalse(costCategoryDao.contains(category.getRefId()));
		Assert.assertTrue(processDao.contains(process.getRefId()));
		processDao.delete(process);
		Assert.assertFalse(processDao.contains(process.getRefId()));
	}

	private Process createProcess(Currency currency, CostCategory category) {
		Process process = new Process();
		process.setRefId(UUID.randomUUID().toString());
		Exchange exchange = new Exchange();
		process.getExchanges().add(exchange);
		exchange.currency = currency;
		exchange.costCategory = category;
		exchange.costFormula = "21 + 21";
		exchange.costValue = 42d;
		process = processDao.insert(process);
		return process;
	}

	private CostCategory createCategory() {
		CostCategory category = new CostCategory();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("Production");
		category = costCategoryDao.insert(category);
		return category;
	}

	private Currency createCurrency() {
		Currency currency = new Currency();
		currency.setRefId(UUID.randomUUID().toString());
		currency.code = "USD";
		currency.conversionFactor = 1.0;
		currency.referenceCurrency = currency;
		currency = currencyDao.insert(currency);
		return currency;
	}

}
