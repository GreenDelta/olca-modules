package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class WasteCalculationTest {

	private final IDatabase db = Tests.getDb();

	private Flow product;
	private Flow waste;
	private Flow elem;

	@Before
	public void setup() {
		var kg = Unit.of("kg");
		var units = db.insert(
				UnitGroup.of("Units of mass", kg));
		var mass = db.insert(
				FlowProperty.of("Mass", units));

		product = db.insert(
				Flow.product("p", mass));
		waste = db.insert(
				Flow.waste("w", mass));
		elem = db.insert(
				Flow.elementary("e", mass));
	}

	@After
	public void tearDown() {
		db.clear();
	}

	@Test
	public void testDownstream() {
		var p = Process.of("p", product);
		p.output(waste, 2.0);
		p = db.insert(p);
		var w = Process.of("w", waste);
		w.output(elem, 2.0);
		w = db.insert(w);
		var system = ProductSystem.of(p)
				.link(p, w);
		var setup = CalculationSetup.fullAnalysis(system);
		var result = new SystemCalculator(db).calculateFull(setup);
		assertEquals(4.0, result.getTotalFlowResults().get(0).value(), 1e-10);
	}

	@Test
	public void testReference() {
		var w = Process.of("w", waste);
		w.input(product, 2.0);
		w = db.insert(w);
		var p = Process.of("p", product);
		p.output(elem, 2.0);
		p = db.insert(p);
		var system = ProductSystem.of(w)
				.link(p, w);
		var setup = CalculationSetup.fullAnalysis(system);
		var result = new SystemCalculator(db).calculateFull(setup);
		assertEquals(4.0, result.getTotalFlowResults().get(0).value(), 1e-10);
	}

}
