package org.openlca.core.results;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AvoidedWasteTest {

	private final IDatabase db = Tests.getDb();
	private LcaResult result;
	private List<RootEntity> entities;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var p = Flow.product("p", mass);
		var w = Flow.waste("w", mass);
		var e = Flow.elementary("e", mass);
		var r = Flow.elementary("r", mass);

		var pP = Process.of("P", p);
		pP.output(w, 0.5).isAvoided = true;
		pP.input(r, 1);
		var wW = Process.of("W", w);
		wW.input(r, 0.5);
		wW.output(e, 2.0);

		db.insert(units, mass, p, w, e, r, pP, wW);
		entities = List.of(units, mass, p, w, e, r, pP, wW);

		var setup = CalculationSetup.of(pP);
		result = new SystemCalculator(db).calculate(setup);
	}

	@After
	public void cleanup() {
		for (var e : entities) {
			db.delete(e);
		}
	}

	@Test
	public void testScaling() {
		assertEquals(1.0, result.getScalingFactorOf(techFlowOf("p")), 1e-10);
		assertEquals(-0.5, result.getScalingFactorOf(techFlowOf("w")), 1e-10);
	}

	@Test
	public void testLci() {
		assertEquals(0.75, result.getTotalFlowValueOf(enviFlowOf("r")), 1e-10);
		assertEquals(-1, result.getTotalFlowValueOf(enviFlowOf("e")), 1e-10);
	}

	@Test
	public void testUpstreamTreeOfR() {
		var u = UpstreamTree.of(result.provider(), enviFlowOf("r"));
		assertEquals(0.75, u.root.result, 1e-10);
		var child = u.childs(u.root).get(0);
		assertEquals(-0.25, child.result, 1e-10);
	}

	@Test
	public void testUpstreamTreeOfE() {
		var u = UpstreamTree.of(result.provider(), enviFlowOf("e"));
		assertEquals(-1.0, u.root.result, 1e-10);
		var child = u.childs(u.root).get(0);
		assertEquals(-1.0, child.result, 1e-10);
	}

	private EnviFlow enviFlowOf(String name) {
		for (var e : result.enviIndex()) {
			if (name.equals(e.flow().name))
				return e;
		}
		throw new AssertionError("flow '" + name + "' not in EnviIndex");
	}

	private TechFlow techFlowOf(String name) {
		for (var t : result.techIndex()) {
			if (name.equals(t.flow().name))
				return t;
		}
		throw new AssertionError("flow '" + name + "' not in TechIndex");
	}
}
