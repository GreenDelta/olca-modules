package org.openlca.io.smartepd;

import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Result;
import org.openlca.core.model.UnitGroup;

public class SmartEpdWriterTest {

	@Test
	public void testWriteEpd() {

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var product = Flow.product("p", mass);

		var epd = new Epd();
		epd.name = "test epd";
		epd.product = EpdProduct.of(product, 1.0);

		var method = ImpactMethod.of("EF 3.1");
		method.code = SmartMethod.EF_3_1.id();
		var gwp = ImpactCategory.of("GWP");
		gwp.code = SmartIndicator.GWP_TOTAL.id();
		method.impactCategories.add(gwp);

		for (var mod : List.of("A1", "A2", "A3")) {
			var result = Result.of(mod, product);
			result.impactMethod = method;
			result.impactResults.add(ImpactResult.of(gwp, 42));
			var module = EpdModule.of(mod, result);
			epd.modules.add(module);
		}

		var smartEpd = SmartEpdWriter.of(epd).write();

		// check the declared unit
		var decUnit = smartEpd.declaredUnit();
		assertNotNull(decUnit);
		assertEquals(1, decUnit.qty(), 1e-9);
		assertEquals("kg", decUnit.unit());
		assertEquals(1.0, smartEpd.massPerUnit(), 1e-9);

		// check the results
		var impacts = smartEpd.resultsOf(SmartIndicatorType.IMPACT)
				.getFirst()
				.results()
				.getFirst();
		assertEquals(method.code, impacts.method());
		assertEquals(gwp.code, impacts.impact());
		var foundMods = EnumSet.noneOf(SmartModule.class);
		for (var val : impacts.values()) {
			foundMods.add(val.module());
			assertEquals(42, val.value(), 1e-9);
		}
		assertEquals(
				EnumSet.of(SmartModule.A1, SmartModule.A2, SmartModule.A3),
				foundMods);
	}

}
