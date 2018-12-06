package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class AllocationIndexTest {

	private IDatabase database = Tests.getDb();

	@Test
	public void testDefaultFactor() {
		FlowDescriptor flow = new FlowDescriptor();
		flow.setId(1);
		ProcessDescriptor process = new ProcessDescriptor();
		process.setId(1);
		Provider provider = Provider.of(process, flow);

		TechIndex index = new TechIndex(provider);
		AllocationIndex allocationIndex = AllocationIndex.create(index,
				AllocationMethod.USE_DEFAULT, MatrixCache.createLazy(database));
		double f = allocationIndex.getFactor(index.getRefFlow(),
				new CalcExchange());
		assertEquals(1.0, f, 1e-16);
	}

}
