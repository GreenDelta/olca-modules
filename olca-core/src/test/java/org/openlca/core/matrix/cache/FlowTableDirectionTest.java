package org.openlca.core.matrix.cache;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class FlowTableDirectionTest {

	private final IDatabase db = Tests.getDb();
	private FlowProperty mass;

	@Before
	public void setUp() {
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		mass = db.insert(FlowProperty.of("Mass", units));
	}

	@After
	public void tearDown() {
		db.delete(mass.unitGroup, mass);
	}

	@Test
	public void testCannotDetermineIt() {
		var bauxite = db.insert(Flow.elementary("Bauxite", mass));
		var co2 = db.insert(Flow.elementary("CO2", mass));
		var directions = FlowTable.directionsOf(db, List.of(
			Descriptor.of(bauxite),
			Descriptor.of(co2)));
		assertEquals(0, directions.get(bauxite.id));
		assertEquals(0, directions.get(co2.id));
	}

	@Test
	public void testDetermineFromExchanges() {
		var bauxite = db.insert(Flow.elementary("Bauxite", mass));
		var co2 = db.insert(Flow.elementary("CO2", mass));
		var pp = db.insert(Flow.product("PP", mass));
		var process = Process.of("p", pp);
		process.input(bauxite, 1.0);
		process.output(co2, 2.0);
		db.insert(process);

		var directions = FlowTable.directionsOf(db, List.of(
			Descriptor.of(bauxite),
			Descriptor.of(co2)));
		assertTrue(directions.get(bauxite.id) < 0);
		assertTrue(directions.get(co2.id) > 0);
		db.delete(bauxite, co2, pp, process);
	}

	@Test
	public void testDetermineFromCategory() {
		var bauxite = Flow.elementary("Bauxite", mass);
		bauxite.category = CategoryDao.sync(db, ModelType.FLOW,
			"Elementary flows/Resource/in ground");
		db.insert(bauxite);
		var co2 = Flow.elementary("CO2", mass);
		co2.category = CategoryDao.sync(db, ModelType.FLOW,
			"Elementary flows/Emissions/to air");
		db.insert(co2);

		var directions = FlowTable.directionsOf(db, List.of(
			Descriptor.of(bauxite),
			Descriptor.of(co2)));
		assertTrue(directions.get(bauxite.id) < 0);
		assertTrue(directions.get(co2.id) > 0);
		db.delete(bauxite, co2);
	}

	@Test
	public void testDetermineFromImpacts() {
		var co2 = Flow.elementary("CO2", mass);
		co2.category = CategoryDao.sync(db, ModelType.FLOW,
			"Elementary flows/Emissions/to air");
		db.insert(co2);
		var nox = db.insert(Flow.elementary("NOx", mass));
		var impact = ImpactCategory.of("I");
		impact.factor(co2, 1);
		impact.factor(nox, 1);
		db.insert(impact);

		var directions = FlowTable.directionsOf(db, List.of(
			Descriptor.of(co2),
			Descriptor.of(nox)));
		assertTrue(directions.get(co2.id) > 0);
		assertTrue(directions.get(nox.id) > 0);
		db.delete(co2, nox);
	}
}
