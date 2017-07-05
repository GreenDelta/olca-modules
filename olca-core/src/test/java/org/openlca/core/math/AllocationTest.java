package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.FullResultProvider;

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

	private void checkIt(AllocationMethod method, Process p) {
		ProductSystem system = TestSystem.of(p).get();
		CalculationSetup setup = new CalculationSetup(system);
		setup.allocationMethod = method;
		FullResultProvider r = TestSystem.calculate(setup);
		assertEquals(1, r.getFlowDescriptors().size());
		FlowDescriptor co2 = r.getFlowDescriptors().iterator().next();
		assertEquals(0.5, r.getTotalFlowResult(co2).value, 1e-16);
	}

}
