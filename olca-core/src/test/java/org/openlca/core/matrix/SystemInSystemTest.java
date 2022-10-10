package org.openlca.core.matrix;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;

public class SystemInSystemTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testCalc() {

		var units = db.insert(UnitGroup.of("Units of mass", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var e = db.insert(Flow.elementary("e", mass));
		var p = db.insert(Flow.product("p", mass));
		var q = db.insert(Flow.product("q", mass));

		// process P
		var procP = Process.of("P", p);
		procP.output(e, 21);
		db.insert(procP);

		// process Q <- P
		var procQ = Process.of("Q", q);
		procQ.input(p, 1.0);
		procQ.output(e, 21);
		db.insert(procQ);

		// system P <- process P
		var sysP = db.insert(ProductSystem.of(procP));

		// system Q <- process Q <- system P
		var sysQ = ProductSystem.of(procQ);
		sysQ.link(TechFlow.of(sysP), procQ);
		db.insert(sysQ);

		var setup = CalculationSetup.of(sysQ);
		var result = new SystemCalculator(db).calculate(setup);
		var ee = EnviFlow.outputOf(e);
		assertEquals(42, result.totalFlowOf(ee), 1e-16);

		db.delete(sysQ, sysP, procP, procQ, p, q, e, mass, units);

	}

}
