package org.openlca.core.matrix.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.CalcImpactFactor;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;

/**
 * Tests that we get all uncertainty information from the matrix-table loaders.
 */
public class UncertaintyTableTest {

	private IDatabase database = Tests.getDb();
	private MatrixCache cache = MatrixCache.createLazy(database);

	@Test
	public void testForExchange() throws Exception {
		Exchange exchange = new Exchange();
		Uncertainty uncertainty = createUncertainty();
		exchange.uncertainty = uncertainty;
		Process process = new Process();
		process.getExchanges().add(exchange);
		ProcessDao dao = new ProcessDao(database);
		dao.insert(process);
		Set<Long> set = new HashSet<>();
		set.add(process.getId());
		List<CalcExchange> exchanges = cache.getExchangeCache().get(
				process.getId());
		checkExchange(exchanges.get(0));
		dao.delete(process);
	}

	private void checkExchange(CalcExchange exchange) {
		Assert.assertEquals(1, exchange.parameter1, 1e-16);
		Assert.assertEquals(2, exchange.parameter2, 1e-16);
		Assert.assertEquals(3, exchange.parameter3, 1e-16);
		Assert.assertEquals("0.5 * 2", exchange.parameter1Formula);
		Assert.assertEquals("4 / 2", exchange.parameter2Formula);
		Assert.assertEquals("4 / 2", exchange.parameter3Formula);
	}

	@Test
	public void testForImpactFactor() throws Exception {
		ImpactFactor factor = new ImpactFactor();
		factor.uncertainty = createUncertainty();
		ImpactCategory category = new ImpactCategory();
		category.impactFactors.add(factor);
		ImpactCategoryDao dao = new ImpactCategoryDao(database);
		dao.insert(category);
		List<CalcImpactFactor> factors = cache.getImpactCache().get(
				category.getId());
		checkFactor(factors.get(0));
		dao.delete(category);
	}

	private void checkFactor(CalcImpactFactor factor) {
		Assert.assertEquals(1, factor.getParameter1(), 1e-16);
		Assert.assertEquals(2, factor.getParameter2(), 1e-16);
		Assert.assertEquals(3, factor.getParameter3(), 1e-16);
	}

	private Uncertainty createUncertainty() {
		Uncertainty uncertainty = Uncertainty.triangle(1, 2, 3);
		uncertainty.setParameter1Formula("0.5 * 2");
		uncertainty.setParameter2Formula("4 / 2");
		uncertainty.setParameter3Formula("4 / 2");
		return uncertainty;
	}

}
