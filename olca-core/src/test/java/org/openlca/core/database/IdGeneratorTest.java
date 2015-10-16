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
		flow.getFlowPropertyFactors().add(factor1);
		Assert.assertEquals(0L, factor1.getId());

		// when inserting an entity we use persist -> EclipseLink directly
		// generates an ID for cascade objects
		database.createDao(Flow.class).insert(flow);
		Assert.assertTrue(factor1.getId() > 0L); // new ID

		FlowPropertyFactor factor2 = new FlowPropertyFactor();
		flow.getFlowPropertyFactors().add(factor2);
		Assert.assertEquals(0L, factor2.getId());

		// when updating an entity we use merge -> EclipseLink does not
		// manipulates the original object graph
		Flow managedFlow = database.createDao(Flow.class).update(flow);
		Assert.assertEquals(0L, factor2.getId()); // still no ID set

		// for the managed flow has generated IDs for the cascade objects
		int count = 0;
		for (FlowPropertyFactor f : managedFlow.getFlowPropertyFactors()) {
			Assert.assertTrue(f.getId() > 0L);
			count++;
		}
		Assert.assertTrue(count == 2);

	}

}
