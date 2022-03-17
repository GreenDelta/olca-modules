package org.openlca.core.database.usage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;

public class FlowUseSearchTest {

	private final IDatabase db = Tests.getDb();
	private final UsageSearch search = UsageSearch.of(ModelType.FLOW, db);
	private Flow flow;

	@Before
	public void setup() {
		flow = new Flow();
		flow.name = "flow";
		flow = db.insert(flow);
	}

	@After
	public void tearDown() {
		db.delete(flow);
	}

	@Test
	public void testFindNoUsage() {
		var models = search.find(flow.id);
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInImpactCategories() {
		var factor = new ImpactFactor();
		factor.flow = flow;
		var impact = new ImpactCategory();
		impact.impactFactors.add(factor);
		db.insert(impact);
		var results = search.find(flow.id);
		db.delete(impact);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(Descriptor.of(impact), results.iterator().next());
	}


	@Test
	public void testFindInProcesses() {
		var process = createProcess();
		var results = search.find(flow.id);
		db.delete(process);
		var expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.iterator().next());
	}

	private Process createProcess() {
		var process = new Process();
		process.name = "process";
		var exchange = new Exchange();
		exchange.flow = flow;
		process.exchanges.add(exchange);
		return db.insert(process);
	}
}
