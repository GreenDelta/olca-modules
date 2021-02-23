package org.openlca.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;


public class DatabasesTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testHasInventoryData() {
		if (Databases.hasInventoryData(db)) {
			db.clear();
		}
		assertFalse(Databases.hasInventoryData(db));

		var kg = Unit.of("kg");
		var units = db.insert(UnitGroup.of("Mass units", kg));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var steel = db.insert(Flow.product("Steel", mass));
		var process = db.insert(Process.of("Steel production", steel));
		assertTrue(Databases.hasInventoryData(db));

		List.of(process, steel, mass, units).forEach(db::delete);
		assertFalse(Databases.hasInventoryData(db));
	}

	@Test
	public void testHasImpactData() {
		if (Databases.hasImpactData(db)) {
			db.clear();
		}
		assertFalse(Databases.hasImpactData(db));

		var kg = Unit.of("kg");
		var units = db.insert(UnitGroup.of("Mass units", kg));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var co2 = db.insert(Flow.elementary("CO2", mass));
		var impact = ImpactCategory.of("GWP", "CO2 eq.");
		impact.factor(co2, 1.0);
		db.insert(impact);
		assertTrue(Databases.hasImpactData(db));

		List.of(impact, co2, mass, units).forEach(db::delete);
		assertFalse(Databases.hasImpactData(db));
	}

}
