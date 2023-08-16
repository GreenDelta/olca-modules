package org.openlca.core.math;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

import java.util.function.Consumer;

public class ImpactDirectionTest {

	private final IDatabase db = Tests.getDb();

	// things are calculated for 1 unit of product p
	// r is the flow used as resource with negative impacts
	// on the environment; thus the value of the result is > 0
	// e is the flow used as emission with positive impacts
	// on the environment; thus the value of the result is < 0
	private Flow p;
	private Flow r;
	private Flow e;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		p = Flow.product("p", mass);
		r = Flow.elementary("r", mass);
		e = Flow.elementary("e", mass);
		db.insert(units, mass, p, r, e);
	}

	@After
	public void cleanup() {
		var mass = p.referenceFlowProperty;
		var units = mass.unitGroup;
		db.delete(p, r, e, mass, units);
	}

	@Test
	public void testPositiveFactorsWithDirection() {
		var result = calc(
				process -> {
					process.input(r, 1);
					process.output(e, 1);
				},
				indicator -> {
					indicator.direction = Direction.INPUT;
					indicator.factor(r, 1);
					indicator.factor(e, 0.5);
				});
		assertEquals(0.5, result, 1e-16);
	}

	@Test
	public void testNegativeFactorWithDirection() {
		var result = calc(
				process -> {
					process.input(r, 1);
					process.output(e, 1);
				},
				indicator -> {
					indicator.direction = Direction.INPUT;
					indicator.factor(r, 1);
					indicator.factor(e, -0.5);
				});
		assertEquals(0.5, result, 1e-16);
	}

	@Test
	public void testNegativeFactorNoDirection() {
		var result = calc(
				process -> {
					process.input(r, 1);
					process.output(e, 1);
				},
				indicator -> {
					indicator.factor(r, 1);
					indicator.factor(e, -0.5);
				});
		assertEquals(0.5, result, 1e-16);
	}

	private double calc(Consumer<Process> p, Consumer<ImpactCategory> i) {
		var process = Process.of("P", this.p);
		p.accept(process);
		var indicator = ImpactCategory.of("I");
		i.accept(indicator);
		var method = ImpactMethod.of("M");
		method.add(indicator);
		db.insert(process, indicator, method);
		var setup = CalculationSetup.of(process).withImpactMethod(method);
		var result = new SystemCalculator(db)
				.calculate(setup)
				.getTotalImpactValueOf(Descriptor.of(indicator));
		db.delete(process, method, indicator);
		return result;
	}
}
