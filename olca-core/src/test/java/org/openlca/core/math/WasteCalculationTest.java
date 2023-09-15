package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

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
		var setup = CalculationSetup.of(system);
		var result = new SystemCalculator(db).calculate(setup);
		assertEquals(4.0, result.getTotalFlows().get(0).value(), 1e-10);
		result.dispose();
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
		var setup = CalculationSetup.of(system);
		var result = new SystemCalculator(db).calculate(setup);
		assertEquals(4.0, result.getTotalFlows().get(0).value(), 1e-10);
		result.dispose();
	}

	@Test
	public void testLinkedWasteResult() {

		// create the process model
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var w = Flow.waste("w", mass);
		var process = Process.of("P", p);
		process.output(w, 2);

		// create the result model
		var result = Result.of("R", w);
		var i = ImpactCategory.of("I");
		var m = ImpactMethod.of("M");
		m.add(i);
		result.impactMethod = m;
		result.impactResults.add(ImpactResult.of(i, 21));

		db.insert(units, mass, p, w, process, i, m, result);

		// create product system & calculate
		var sys = ProductSystem.of("Sys", process);
		sys.link(TechFlow.of(result), process);
		db.insert(sys);
		var setup = CalculationSetup.of(sys).withImpactMethod(m);
		var r = new SystemCalculator(db).calculate(setup);
		var v = r.getTotalImpactValueOf(Descriptor.of(i));

		db.delete(sys, result, m, i, process, p, w, mass, units);
		assertEquals(42, v, 1e-16);
	}

}
