package org.openlca.core.database.usage;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

public class FlowPropertyFactorUseSearchTest {

	private IDatabase database = Tests.getDb();
	private FlowPropertyFactorUseSearch search;
	private Flow flow;
	private FlowProperty property;
	private FlowPropertyFactor factor;

	@Before
	public void setup() {
		flow = new Flow();
		flow.name = "flow";
		property = new FlowProperty();
		property.name = "property";
		factor = new FlowPropertyFactor();
		factor.flowProperty = property;
		flow.flowPropertyFactors.add(factor);
		property = new FlowPropertyDao(database).insert(property);
		flow = new FlowDao(database).insert(flow);
		factor = flow.getFactor(property);
		search = new FlowPropertyFactorUseSearch(flow, database);
	}

	@After
	public void tearDown() {
		new FlowDao(database).delete(flow);
		new FlowPropertyDao(database).delete(property);
	}

	@Test
	public void testFindNoUsage() {
		List<CategorizedDescriptor> models = search.findUses(factor);
		Assert.assertNotNull(models);
		Assert.assertTrue(models.isEmpty());
	}

	@Test
	public void testFindInImpactCategory() {
		ImpactFactor iFactor = new ImpactFactor();
		iFactor.flow = flow;
		iFactor.flowPropertyFactor = factor;
		ImpactCategory category = new ImpactCategory();
		category.impactFactors.add(iFactor);
		ImpactCategoryDao dao = new ImpactCategoryDao(database);
		dao.insert(category);
		List<CategorizedDescriptor> results = search.findUses(factor);
		dao.delete(category);
		Assert.assertEquals(1, results.size());
		Descriptor expected = Descriptor.of(category);
		Assert.assertEquals(expected, results.get(0));
	}

	@Test
	public void testFindInProcesses() {
		Process process = createProcess();
		List<CategorizedDescriptor> results = search.findUses(factor);
		new ProcessDao(database).delete(process);
		Descriptor expected = Descriptor.of(process);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private Process createProcess() {
		Process process = new Process();
		process.name = "process";
		Exchange exchange = new Exchange();
		exchange.flow = flow;
		exchange.flowPropertyFactor = factor;
		process.exchanges.add(exchange);
		return new ProcessDao(database).insert(process);
	}
}
