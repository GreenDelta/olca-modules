package org.openlca.core.math;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.UpstreamTree;

public class CostTests {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSingleProcess() {
		var process = TestProcess
				.refProduct("p1", 1, "kg")
				.addCosts("p1", 2, "EUR")
				.elemIn("water", 1, "m3")
				.addCosts("water", 5, "EUR")
				.get();
		var system = TestSystem.of(process).get();
		var result = TestSystem.calculate(system);

		var techFlow = TechFlow.of(process);

		assertEquals(3, result.totalCosts(), 1e-10);
		assertEquals(3, result.totalCostsOf(techFlow), 1e-10);
		assertEquals(3, result.directCostsOf(techFlow), 1e-10);
		var tree = UpstreamTree.costsOf(result.provider());
		var root = tree.root;
		assertTrue(tree.childs(root).isEmpty());
		assertEquals(3, root.result(), 1e-10);
		var contributions = result.getProcessCostContributions();
		assertEquals(1, contributions.size());
		var c = contributions.get(0);
		assertEquals(3, c.amount, 1e-10);
		assertEquals(1, c.share, 1e-10);
	}

	@Test
	public void testSimpleContribution() {
		Process p1 = TestProcess
				.refProduct("p1", 1, "kg")
				.addCosts("p1", 2, "EUR")
				.elemIn("water", 1, "m3")
				.addCosts("water", 5, "EUR")
				.get();
		ProductSystem system = TestSystem.of(p1).get();
		LcaResult r = TestSystem.calculate(system);
		assertEquals(3, r.totalCosts(), 1e-10);
	}

	@Test
	public void testSimpleChain() {
		Process p1 = TestProcess
				.refProduct("p1", 1, "kg")
				.addCosts("p1", 2, "EUR")
				.elemIn("water", 1, "m3")
				.addCosts("water", 5, "EUR")
				.get();
		Process p2 = TestProcess
				.refProduct("p2", 1, "kg")
				.addCosts("p2", 5, "EUR")
				.prodIn("p1", 2, "kg")
				.addCosts("p1", 4, "EUR")
				.get();
		ProductSystem system = TestSystem.of(p2).link(p1).get();
		LcaResult r = TestSystem.calculate(system);

		var d1 = TechFlow.of(p1);
		var d2 = TechFlow.of(p2);

		assertEquals(5, r.totalCosts(), 1e-10);
		assertEquals(6, r.directCostsOf(d1), 1e-10);
		assertEquals(-1, r.directCostsOf(d2), 1e-10);
		assertEquals(6, r.totalCostsOf(d1), 1e-10);
		assertEquals(5, r.totalCostsOf(d2), 1e-10);
	}

	@Test
	public void testAddCostsForSameProduct() {
		Process p1 = TestProcess
				.refProduct("p1", 1, "kg")
				.addCosts("p1", 1, "EUR")
				.prodIn("p1", 0.5, "kg")
				.elemIn("water", 1, "m3")
				.get();
		long exchangeId = -1;
		for (Exchange e : p1.exchanges) {
			if (e.flow.name.equals("p1") && e.isInput) {
				e.costs = 0.4d;
				e.currency = p1.quantitativeReference.currency;
				exchangeId = e.id;
				break;
			}
		}
		p1 = db.update(p1);

		ProductSystem system = TestSystem.of(p1).get();
		// add a link to the process itself
		ProcessLink selfLink = new ProcessLink();
		selfLink.flowId = p1.quantitativeReference.flow.id;
		selfLink.providerId = p1.id;
		selfLink.processId = p1.id;
		selfLink.exchangeId = exchangeId;
		system.processLinks.add(selfLink);
		system = db.update(system);

		LcaResult r = TestSystem.calculate(system);
		assertEquals(-1.2, r.totalCosts(), 1e-10);
	}

	@Test
	public void test() {

		Process electricity = TestProcess
				.refProduct("Electricity", 1, "MJ")
				.addCosts("Electricity", 5, "EUR")
				.elemOut("CO2", 3, "kg")
				.get();

		Process wood = TestProcess
				.refProduct("Wood", 1, "kg")
				.addCosts("Wood", 1, "EUR")
				.get();

		Process chair = TestProcess
				.refProduct("Chair", 1, "piece")
				.addCosts("Chair", 25, "EUR")
				.prodIn("Electricity", 2, "MJ")
				.addCosts("Electricity", 10, "EUR")
				.prodIn("Wood", 5, "kg")
				.addCosts("Wood", 5, "EUR")
				.get();

		Process disposal = TestProcess
				.refProduct("Disposal of chair", 1, "piece")
				.addCosts("Disposal of chair", 2, "EUR")
				.get();

		Process usage = TestProcess
				.refProduct("Sitting", 10, "years")
				.addCosts("Sitting", 135, "EUR")
				.prodIn("Chair", 5, "piece")
				.addCosts("Chair", 125, "EUR")
				.prodIn("Disposal of chair", 5, "piece")
				.addCosts("Disposal of chair", 10, "EUR")
				.get();

		ProductSystem system = TestSystem
				.of(usage)
				.link(disposal)
				.link(chair)
				.link(wood)
				.link(electricity)
				.get();

		// TODO: test something here
		LcaResult result = TestSystem.calculate(system);
		System.out.println(result.totalCosts());

	}

}
