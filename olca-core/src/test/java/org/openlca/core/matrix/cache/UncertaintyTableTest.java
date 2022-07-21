package org.openlca.core.matrix.cache;


import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;

/**
 * Tests that we get all uncertainty information from the matrix-table loaders.
 */
public class UncertaintyTableTest {

	private final IDatabase db = Tests.getDb();
	private final MatrixCache cache = MatrixCache.createLazy(db);

	@Test
	public void testForExchange() throws Exception {

		var exchange = new Exchange();
		exchange.uncertainty = Uncertainty.triangle(1, 2, 3);
		var process = new Process();
		process.exchanges.add(exchange);
		db.insert(process);

		var e = cache.getExchangeCache()
			.get(process.id)
			.get(0);
		assertEquals(1, e.parameter1, 1e-16);
		assertEquals(2, e.parameter2, 1e-16);
		assertEquals(3, e.parameter3, 1e-16);

		db.delete(process);
	}
}
