package org.openlca.io.smartepd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.DataDir;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;

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

	@Test
	public void testMapIndicatorCodesInWriter() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var epd = Epd.of("test epd", p);

		var result = Result.of("A1", p);
		epd.modules.add(EpdModule.of("A1", result));
		var method = ImpactMethod.of("EF 3.1");
		method.code = SmartMethod.EF_3_1.id();
		result.impactMethod = method;

		var values = new EnumMap<SmartIndicator, Double>(SmartIndicator.class);
		var rand = ThreadLocalRandom.current();
		for (var i : SmartIndicator.values()) {
			var impact = ImpactCategory.of("I1", i.unitFor(SmartMethod.EF_3_1));
			impact.code = i.id();
			double value = rand.nextDouble();
			values.put(i, value);
			var r = ImpactResult.of(impact, value);
			result.impactResults.add(r);
		}

		var smart = SmartEpdWriter.of(epd).write();

		for (var i : SmartIndicator.values()) {
			var impact = smart.resultListsOf(i.type())
					.getFirst()
					.results()
					.stream()
					.filter(r -> {
						var id = i.isImpact() ? r.impact() : r.indicator();
						return id.equals(i.id());
					}).findFirst()
					.orElseThrow()
					.values()
					.getFirst();
			assertEquals("A1", impact.module().name());
			assertEquals(values.get(i), impact.value(), 1e-9);
		}
	}

	@Test
	public void testMapMethodCodesInWriter() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);

		for (var m : SmartMethod.values()) {
			var epd = Epd.of("test epd", p);
			var result = Result.of("A1", p);
			epd.modules.add(EpdModule.of("A1", result));
			var method = ImpactMethod.of("M");
			method.code = m.id();
			result.impactMethod = method;
			var impact = ImpactCategory.of(
					"I1", SmartIndicator.GWP_TOTAL.defaultUnit());
			impact.code = SmartIndicator.GWP_TOTAL.id();
			var value = ThreadLocalRandom.current().nextDouble();
			var r = ImpactResult.of(impact, value);
			result.impactResults.add(r);

			var smart = SmartEpdWriter.of(epd).write();
			var smartImpact = smart.resultListsOf(SmartIndicatorType.IMPACT)
					.getFirst()
					.results()
					.stream()
					.filter(res -> res.method().equals(m.id()))
					.findFirst()
					.orElseThrow();
			assertEquals(m.id(), smartImpact.method());
			assertEquals(SmartIndicator.GWP_TOTAL.id(), smartImpact.impact());
			var vs = smartImpact.values();
			assertEquals(1, vs.size());
			assertEquals(value, vs.getFirst().value(), 1e-9);
			assertEquals("A1", vs.getFirst().module().name());
		}
	}
}
