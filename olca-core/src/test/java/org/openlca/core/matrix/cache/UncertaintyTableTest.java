package org.openlca.core.matrix.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.model.Exchange;
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
		process.exchanges.add(exchange);
		ProcessDao dao = new ProcessDao(database);
		dao.insert(process);
		Set<Long> set = new HashSet<>();
		set.add(process.id);
		List<CalcExchange> exchanges = cache.getExchangeCache().get(
				process.id);
		checkExchange(exchanges.get(0));
		dao.delete(process);
	}

	private void checkExchange(CalcExchange e) {
		Assert.assertEquals(1, e.parameter1, 1e-16);
		Assert.assertEquals(2, e.parameter2, 1e-16);
		Assert.assertEquals(3, e.parameter3, 1e-16);
	}

	private Uncertainty createUncertainty() {
		Uncertainty uncertainty = Uncertainty.triangle(1, 2, 3);
		uncertainty.formula1 = "0.5 * 2";
		uncertainty.formula2 = "4 / 2";
		uncertainty.formula3 = "4 / 2";
		return uncertainty;
	}

}
