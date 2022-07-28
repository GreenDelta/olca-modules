package org.openlca.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;

public class AllocationPropertyTest {

	@Test
	public void testSelectProperties() {
		var massUnits = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", massUnits);
		var energyUnits = UnitGroup.of("Energy units", "MJ");
		var energy = FlowProperty.of("Energy", energyUnits);

		var p = Flow.product("p", mass);
		p.property(energy, 42);
		var w = Flow.waste("w", energy);
		w.property(mass, 1.0 / 24.0);
		var e = Flow.elementary("e", mass);

		var process = Process.of("P", p);
		process.input(w, 1);
		process.output(e, 1);

		var props = AllocationUtils.allocationPropertiesOf(process);
		assertEquals(2, props.size());
		assertTrue(props.contains(mass));
		assertTrue(props.contains(energy));
	}
}
