package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;

public class AllocationIndexTest {

	private IDatabase database = TestSession.getDefaultDatabase();

	@Test
	public void testDefaultFactor() {
		ProductIndex index = new ProductIndex(
				LongPair.of(999999999, 999999999), 1);
		AllocationIndex allocationIndex = AllocationIndex.create(index,
				AllocationMethod.USE_DEFAULT, MatrixCache.create(database));
		double f = allocationIndex.getFactor(index.getRefProduct(),
				new CalcExchange());
		assertEquals(1.0, f, 1e-16);
	}

}
