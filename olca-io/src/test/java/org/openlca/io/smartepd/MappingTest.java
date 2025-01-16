package org.openlca.io.smartepd;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.core.model.ImpactMethod;

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

	/// This test checks that all impact indicators are defined for the respective
	/// method mappings because these are used to identify the method for an EPD.
	/// This test is ignored because it uses a database that must contain all
	/// methods defined in the standard method mappings.
	@Test
	@Ignore
	public void testMethodIndicators() {
		try (var db = DataDir.get().openDatabase("example_epds")) {
			var methodMappings = SmartMethodMapping.getDefault();
			var indicatorMappings = SmartIndicatorMapping.getDefault();

			for (var method : db.getAll(ImpactMethod.class)) {
				var methodMapping = methodMappings.stream()
						.filter(m -> m.ref().id().equals(method.refId))
						.findFirst()
						.orElseThrow();

				for (var i : method.impactCategories) {
					var indicatorMapping = indicatorMappings.stream()
							.filter(m -> m.refs().stream().anyMatch(r -> r.id().equals(i.refId)))
							.findFirst()
							.orElse(null);
					if (indicatorMapping == null) {
						System.out.println("no mapping for indicator "
								+ i.name + " in method " + method.name);
						continue;
					}
					if (!indicatorMapping.indicator().isImpact())
						continue;
					assertTrue("indicator " + i.refId + " in method " + method.name,
							methodMapping.indicators().contains(i.refId));
				}
			}
		}
	}

}
