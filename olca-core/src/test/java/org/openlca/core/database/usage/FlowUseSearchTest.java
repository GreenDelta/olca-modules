package org.openlca.core.database.usage;

import java.util.List;

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
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;

public class FlowUseSearchTest {

	private IDatabase database = Tests.getDb();
	private IUseSearch<FlowDescriptor> search;
	private Flow flow;

	@Before
	public void setup() {
		flow = new Flow();
		flow.setName("flow");
		flow = database.createDao(Flow.class).insert(flow);
		search = IUseSearch.FACTORY.createFor(ModelType.FLOW, database);
	}

	@After
	public void tearDown() {
		database.createDao(Flow.class).delete(flow);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(Descriptors
				.toDescriptor(flow));
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInImpactMethods() {
		ImpactMethod method = createMethod();
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(flow));
		database.createDao(ImpactMethod.class).delete(method);
		BaseDescriptor expected = Descriptors.toDescriptor(method);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private ImpactMethod createMethod() {
		ImpactMethod method = new ImpactMethod();
		method.setName("method");
		ImpactFactor iFactor = new ImpactFactor();
		iFactor.flow = flow;
		ImpactCategory category = new ImpactCategory();
		category.impactFactors.add(iFactor);
		method.impactCategories.add(category);
		return database.createDao(ImpactMethod.class).insert(method);
	}

	@Test
	public void testFindInProcesses() {
		Process process = createProcess();
		List<CategorizedDescriptor> results = search.findUses(Descriptors
				.toDescriptor(flow));
		database.createDao(Process.class).delete(process);
		BaseDescriptor expected = Descriptors.toDescriptor(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		process.setName("process");
		Exchange exchange = new Exchange();
		final Flow flow1 = flow;
		exchange.flow = flow1;
		process.getExchanges().add(exchange);
		return database.createDao(Process.class).insert(process);
	}
}
