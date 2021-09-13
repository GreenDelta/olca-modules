package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Process;

public class AllocationTest {

	@Test
	public void testProducts() {
		AllocationMethod method = AllocationMethod.PHYSICAL;
		Process p = TestProcess
				.refProduct("p1", 1, "kg")
				.prodOut("p2", 3, "kg")
				.elemOut("CO2", 2, "kg")
				.alloc("p1", method, 0.25)
				.alloc("p2", method, 0.75)
				.get();
		checkIt(method, p);
	}

	@Test
	public void testWastes() {
		AllocationMethod method = AllocationMethod.PHYSICAL;
		Process p = TestProcess
				.refWaste("w1", 1, "kg")
				.wasteIn("w2", 3, "kg")
				.elemOut("CO2", 2, "kg")
				.alloc("w1", method, 0.25)
				.alloc("w2", method, 0.75)
				.get();
		checkIt(method, p);
	}

	@Test
	public void testProductWaste() {
		AllocationMethod method = AllocationMethod.PHYSICAL;
		Process p = TestProcess
				.refProduct("p1", 1, "kg")
				.wasteIn("w2", 3, "kg")
				.elemOut("CO2", 2, "kg")
				.alloc("p1", method, 0.25)
				.alloc("w2", method, 0.75)
				.get();
		checkIt(method, p);
	}

	@Test
	public void testWasteProduct() {
		AllocationMethod method = AllocationMethod.PHYSICAL;
		Process p = TestProcess
				.refWaste("w1", 1, "kg")
				.prodOut("p2", 3, "kg")
				.elemOut("CO2", 2, "kg")
				.alloc("w1", method, 0.25)
				.alloc("p2", method, 0.75)
				.get();
		checkIt(method, p);
	}

	@Test
	public void testFormula() {
		var method = AllocationMethod.PHYSICAL;
		var p = TestProcess
				.refProduct("p1", 1, "kg")
				.wasteIn("w2", 3, "kg")
				.elemOut("CO2", 2, "kg")
				.param("param", 25)
				.alloc("p1", method, "param / 100")
				.alloc("w2", method, "3 * param / 100")
				.get();
		checkIt(method, p);
	}

	private void checkIt(AllocationMethod method, Process p) {
		var system = TestSystem.of(p).get();
		var setup = CalculationSetup.fullAnalysis(system)
			.withAllocation(method);
		var result = TestSystem.calculate(setup);
		assertEquals(1, result.enviIndex().size());
		var co2 = result.enviIndex().at(0);
		assertEquals(0.5, result.getTotalFlowResult(co2), 1e-16);
	}
}
