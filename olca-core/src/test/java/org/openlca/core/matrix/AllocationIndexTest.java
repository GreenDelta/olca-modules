package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
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
		ProcessProduct provider = ProcessProduct.of(process, flow);

		TechIndex index = new TechIndex(provider);
		AllocationIndex allocationIndex = AllocationIndex.create(database,
				index,
				AllocationMethod.USE_DEFAULT);
		double f = allocationIndex.get(index.getRefFlow(), 1);
		assertEquals(1.0, f, 1e-16);
	}

}
