package org.openlca.core.database;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.CalculationType;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CalculationSetupIOTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testEmpty() {
		var setup = new CalculationSetup();
		assertEquals(0L, setup.id);
		db.insert(setup);
		assertTrue(setup.id > 0);
		db.delete(setup);
	}

	@Test
	public void testFull() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var flow = Flow.product("Steel", mass);
		var process = Process.of("Steel production", flow);

		var setup = CalculationSetup.contributions(process)
			.withAllocation(AllocationMethod.ECONOMIC)
			.withAmount(42.0)
			.withRegionalization(true);

		db.insert(units, mass, flow, process, setup);
		db.clearCache();

		var clone = db.get(CalculationSetup.class, setup.id);
		assertEquals(setup, clone);
		assertEquals(units.referenceUnit, clone.unit());
		assertEquals(mass, clone.flowPropertyFactor().flowProperty);
		assertEquals(flow, clone.flow());
		assertEquals(process, clone.process());
		assertEquals(CalculationType.CONTRIBUTION_ANALYSIS, clone.type());
		assertEquals(AllocationMethod.ECONOMIC, clone.allocation());
		assertEquals(42.0, clone.amount(), 1e-10);
		assertTrue(clone.hasRegionalization());

		db.delete(setup, process, flow, mass, units);
	}
}
