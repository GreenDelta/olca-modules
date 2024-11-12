package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class AllocateAvoidedTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testAllocateAvoided() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var a = Flow.product("p", mass);
		var e = Flow.elementary("e", mass);
		db.insert(units, mass, p, q, a, e);

		var P = Process.of("P", p);
		P.output(q, 1.0);
		P.input(a, 0.5).isAvoided = true;
		P.output(e, 2.0);
		P.allocationFactors.addAll(List.of(
				AllocationFactor.physical(p, 0.75),
				AllocationFactor.physical(q, 0.25)
		));
		var A = Process.of("A", a);
		A.output(e, 1.5);
		db.insert(P, A);

		var setup = CalculationSetup.of(P)
				.withAllocation(AllocationMethod.PHYSICAL);
		var r = new SystemCalculator(db).calculate(setup);
		var ef = r.enviIndex().stream()
				.filter(ei -> ei.flow().id == e.id)
				.findAny()
				.orElseThrow();
		var value = r.getTotalFlowValueOf(ef);

		db.delete(A, P, e, a, q, p, mass, units);

		// 0.75 * 2.0 - 0.75 * 0.5 * 1.5 = 0.9375
		assertEquals(0.9375, value, 1e-9);
	}


}
