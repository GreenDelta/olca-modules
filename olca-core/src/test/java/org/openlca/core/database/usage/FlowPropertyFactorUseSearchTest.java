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
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptors;

public class FlowPropertyFactorUseSearchTest {

	private IDatabase database = Tests.getDb();
	private FlowPropertyFactorUseSearch search;
	private Flow flow;
	private FlowProperty property;
	private FlowPropertyFactor factor;

	@Before
	public void setup() {
		flow = new Flow();
		flow.setName("flow");
		property = new FlowProperty();
		property.setName("property");
		factor = new FlowPropertyFactor();
		factor.setFlowProperty(property);
		flow.getFlowPropertyFactors().add(factor);
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
	public void testFindInImpactMethods() {
		ImpactMethod method = createMethod();
		List<CategorizedDescriptor> results = search.findUses(factor);
		new ImpactMethodDao(database).delete(method);
		BaseDescriptor expected = Descriptors.toDescriptor(method);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(expected, results.get(0));
	}

	private ImpactMethod createMethod() {
		ImpactMethod method = new ImpactMethod();
		method.setName("method");
		ImpactFactor iFactor = new ImpactFactor();
		iFactor.flow = flow;
		iFactor.flowPropertyFactor = factor;
		ImpactCategory category = new ImpactCategory();
		category.impactFactors.add(iFactor);
		method.impactCategories.add(category);
		return new ImpactMethodDao(database).insert(method);
	}

	@Test
	public void testFindInProcesses() {
		Process process = createProcess();
		List<CategorizedDescriptor> results = search.findUses(factor);
		new ProcessDao(database).delete(process);
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
		exchange.flowPropertyFactor = factor;
		process.getExchanges().add(exchange);
		return new ProcessDao(database).insert(process);
	}
}
