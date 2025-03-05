package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;

public class AvoidedFlowsTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testProduct() {
		Process p1 = TestProcess
				.refProduct("p1", 1, "kg")
				.prodIn("p2", 0.5, "kg")
				.elemOut("CO2", 2.0, "kg")
				.get();
		TestProcess.findExchange(p1, "p2").isAvoided = true;
		p1 = db.update(p1);
		Process p2 = TestProcess
				.refProduct("p2", 1.0, "kg")
				.elemOut("CO2", 2.0, "kg")
				.get();
		check(p1, p2);
	}

	@Test
	public void testWaste() {
		Process p = TestProcess
				.refProduct("p", 1, "kg")
				.wasteOut("w", 0.5, "kg")
				.elemOut("CO2", 2.0, "kg")
				.get();
		TestProcess.findExchange(p, "w").isAvoided = true;
		p = db.update(p);
		Process w = TestProcess
				.refWaste("w", 1.0, "kg")
				.elemOut("CO2", 2.0, "kg")
				.get();
		check(p, w);
	}

	private void check(Process refProc, Process linkedProc) {
		var system = TestSystem.of(refProc).link(linkedProc).get();
		var r = TestSystem.calculate(system);
		assertEquals(1, r.enviIndex().size());
		var co2 = r.enviIndex().at(0);
		assertEquals(1.0, r.getTotalFlowValueOf(co2), 1e-16);
		r.dispose();
	}

}
