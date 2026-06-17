package org.openlca.core.matrix;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.results.EnviFlowValue;

public class ProductSystemBuilderTest {

	private final IDatabase db = Tests.getDb();

	private Process A;
	private Process B;
	private Process C;
	private Flow e;

	@Before
	public void setup() {
		var units = UnitGroup.of("Units of mass", "kg");
		var mass = FlowProperty.of("Mass", units);

		Flow a = Flow.product("a", mass);
		Flow b = Flow.product("b", mass);
		Flow c = Flow.product("c", mass);
		Flow d = Flow.product("d", mass);
		e = Flow.elementary("e", mass);
		db.insert(units, mass, a, b, c, d, e);

		A = Process.of("A", a);
		A.input(b, 1.0);
		A.input(c, 1.0);
		A.output(e, 1.0);
		A = db.insert(A);

		B = Process.of("B", b);
		B.input(d, 1.0);
		B.output(e, 1.0);
		B = db.insert(B);

		C = Process.of("C", c);
		C.input(d, 1.0);
		C.output(e, 1.0);
		C = db.insert(C);

		var D  = Process.of("D", d);
		D.output(e, 1.0);
		db.insert(D);
	}

	@After
	public void tearDown() {
		db.clear();
	}

	@Test
	public void testNoLinks() {
		var system = ProductSystem.of(A);
		assertInventoryResultOf(system, 1.0);
	}

	@Test
	public void testLinkFromRoot() {
		var system = ProductSystem.of(A);
		var builder = new ProductSystemBuilder(db);
		builder.autoComplete(system);
		assertInventoryResultOf(system, 5.0);
	}

	@Test
	public void testLinkIntermediateNodes() {
		var system = ProductSystem.of(A)
			.link(B, A)
			.link(C, A);
		var builder = new ProductSystemBuilder(db);
		builder.autoComplete(system, List.of(
			TechFlow.of(B),
			TechFlow.of(C)));
		assertInventoryResultOf(system, 5.0);
	}

	@Test
	public void testLinkIntermediateNode() {
		var system = ProductSystem.of(A).link(B, A);
		var builder = new ProductSystemBuilder(db);
		builder.autoComplete(system, List.of(TechFlow.of(B)));
		assertInventoryResultOf(system, 3.0);
	}

	private void assertInventoryResultOf( ProductSystem system, double expected) {
		var result = new SystemCalculator(db).calculate(CalculationSetup.of(system));
		double value = result.getTotalFlows().stream()
			.filter(v -> v.enviFlow().flow().id == e.id)
			.mapToDouble(EnviFlowValue::value)
			.sum();
		assertEquals(expected, value, 1e-10);
		result.dispose();
	}

}
