package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;

public class AllocationIndexTest {

	private IDatabase database = Tests.getDb();

	@Test
	public void testDefaultFactor() {
		TechIndex index = new TechIndex(
				LongPair.of(999999999, 999999999));
		AllocationIndex allocationIndex = AllocationIndex.create(index,
				AllocationMethod.USE_DEFAULT, MatrixCache.createLazy(database));
		double f = allocationIndex.getFactor(index.getRefFlow(),
				new CalcExchange());
		assertEquals(1.0, f, 1e-16);
	}

}
