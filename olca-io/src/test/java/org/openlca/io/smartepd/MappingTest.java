package org.openlca.io.smartepd;

import static org.junit.Assert.*;

import org.junit.Test;

public class MappingTest {

	@Test
	public void testMethodMappings() {
		var mappings = SmartMethodMapping.getDefault();
		assertFalse(mappings.isEmpty());
		for (var m : mappings) {
			assertNotNull(m.method());
			assertNotNull(m.ref());
			assertFalse(m.indicators().isEmpty());
		}
	}

	@Test
	public void testIndicatorMappings() {
		var mappings = SmartIndicatorMapping.getDefault();
		assertFalse(mappings.isEmpty());
		for (var m : mappings) {
			assertNotNull(m.indicator());
			assertFalse(m.refs().isEmpty());
		}
	}

	@Test
	public void testIndicatorUnit() {
		// different units for same indicator in different methods
		assertEquals("mol H+ eq", SmartIndicator.AP.unitFor(SmartMethod.EF_3_0));
		assertEquals("mol H+ eq", SmartIndicator.AP.unitFor(SmartMethod.EF_3_1));
		assertEquals("kg SO2 eq", SmartIndicator.AP.unitFor(SmartMethod.CML_2016));
		assertEquals("kg SO2 eq", SmartIndicator.AP.unitFor(SmartMethod.TRACI_2_1));

		// there is only one default unit for resources and output flows
		assertEquals("kg", SmartIndicator.MER.unitFor(SmartMethod.EF_3_0));
		assertEquals("kg", SmartIndicator.MER.defaultUnit());

	}

}
