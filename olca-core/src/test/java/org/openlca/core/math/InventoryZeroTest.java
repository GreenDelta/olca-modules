package org.openlca.core.math;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class InventoryZeroTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testEnviToZero() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var e = Flow.elementary("e", mass);
		var process = Process.of("P", p);
		process.input(e, 1);
		process.output(e, 1);
		db.insert(units, mass, p, e, process);

		var setup = CalculationSetup.of(process);
		var result = new SystemCalculator(db)
				.calculate(setup);
		var enviFlow = result.enviIndex().at(0);
		var enviValue = result.getTotalFlowValueOf(enviFlow);
		assertEquals(0.0, enviValue, 1e-16);

		db.delete(process, p, e, mass, units);
	}

}
