package org.openlca.core.results;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class LinkedResultTest {

	private final IDatabase db = Tests.getDb();
	private CalculationSetup setup;
	private ImpactDescriptor impact;
	private TechFlow resultFlow;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product1 = Flow.product("Product 1", mass);
		var product2 = Flow.product("Product 2", mass);
		var process = Process.of("process", product1);
		process.input(product2, 2);

		var impact = ImpactCategory.of("GWP 100", "CO2eq");
		var method = ImpactMethod.of("Method");
		method.impactCategories.add(impact);
		var result = Result.of("result", product2);
		result.impactMethod = method;
		result.impactResults.add(ImpactResult.of(impact, 21));

		db.insert(
			units,
			mass,
			product1,
			product2,
			process,
			impact,
			method,
			result);

		var system = ProductSystem.of(process);
		system.processes.add(result.id);
		var link = new ProcessLink();
		link.setProviderType(ModelType.RESULT);
		link.providerId = result.id;
		link.flowId = product2.id;
		link.processId = process.id;
		link.exchangeId = process.exchanges.stream()
			.filter(e -> e.isInput)
			.mapToLong(e -> e.id)
			.findAny()
			.orElse(0);
		system.processLinks.add(link);
		db.insert(system);

		setup = CalculationSetup.fullAnalysis(system)
			.withImpactMethod(method);
		this.impact = Descriptor.of(impact);
		this.resultFlow = TechFlow.of(result);
	}

	@After
	public void tearDown() {
		db.clear();
	}

	@Test
	public void testSimpleResult() {
		var calculator = new SystemCalculator(db);
		var r = calculator.calculateSimple(setup);
		assertEquals(42.0, r.getTotalImpactResult(impact), 1e-10);
	}

	@Test
	public void testContributionResult() {
		var calculator = new SystemCalculator(db);
		var r = calculator.calculateContributions(setup);
		assertEquals(42.0, r.getTotalImpactResult(impact), 1e-10);
		assertEquals(42.0, r.getDirectImpactResult(resultFlow, impact), 1e-10);
	}

	@Test
	public void testFullResult() {
		var calculator = new SystemCalculator(db);
		var r = calculator.calculateFull(setup);
		assertEquals(42.0, r.getTotalImpactResult(impact), 1e-10);
		assertEquals(42.0, r.getDirectImpactResult(resultFlow, impact), 1e-10);
		assertEquals(42.0, r.getUpstreamImpactResult(resultFlow, impact), 1e-10);

		// test the upstream tree
		var tree = r.getTree(impact);
		assertEquals(42.0, tree.root.result, 1e-10);
		assertEquals(42.0, tree.childs(tree.root).get(0).result, 1e-10);
	}

}
