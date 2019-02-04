package org.openlca.core.database;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;

public class IdGeneratorTest {

	@Test
	public void testCascadeId() {

		IDatabase database = Tests.getDb();

		Flow flow = new Flow();
		FlowPropertyFactor factor1 = new FlowPropertyFactor();
		flow.flowPropertyFactors.add(factor1);
		Assert.assertEquals(0L, factor1.id);

		// when inserting an entity we use persist -> EclipseLink directly
		// generates an ID for cascade objects
		new FlowDao(database).insert(flow);
		Assert.assertTrue(factor1.id > 0L); // new ID

		FlowPropertyFactor factor2 = new FlowPropertyFactor();
		flow.flowPropertyFactors.add(factor2);
		Assert.assertEquals(0L, factor2.id);

		// when updating an entity we use merge -> EclipseLink does not
		// manipulates the original object graph
		Flow managedFlow = new FlowDao(database).update(flow);
		Assert.assertEquals(0L, factor2.id); // still no ID set

		// for the managed flow has generated IDs for the cascade objects
		int count = 0;
		for (FlowPropertyFactor f : managedFlow.flowPropertyFactors) {
			Assert.assertTrue(f.id > 0L);
			count++;
		}
		Assert.assertTrue(count == 2);

	}

}
