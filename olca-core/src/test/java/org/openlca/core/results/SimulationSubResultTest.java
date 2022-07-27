package org.openlca.core.results;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.Simulator;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UnitGroup;

import static org.junit.Assert.*;

public class SimulationSubResultTest {

	private static final IDatabase db = Tests.getDb();
	private UnitGroup units;
	private FlowProperty mass;
	private Flow e, p, q;

	@Before
	public void setup() {
		units = db.insert(UnitGroup.of("Units of mass", "kg"));
		mass = db.insert(FlowProperty.of("Mass", units));
		e = db.insert(Flow.elementary("e", mass));
		p = db.insert(Flow.product("p", mass));
		q = db.insert(Flow.product("q", mass));
	}

	@After
	public void cleanup() {
		db.delete(q, p, e, mass, units);
	}

	@Test
	public void testFlowInSub() {

		// process P
		var procP = Process.of("P", p);
		procP.output(e, 42).uncertainty = Uncertainty.uniform(21, 63);
		db.insert(procP);

		// process Q <- P
		var procQ = Process.of("Q", q);
		procQ.input(p, 1.0);
		db.insert(procQ);

		// system P <- process P
		var sysP = db.insert(ProductSystem.of(procP));

		// system Q <- process Q <- system P
		var sysQ = ProductSystem.of(procQ);
		sysQ.link(TechFlow.of(sysP), procQ);
		db.insert(sysQ);

		var ee = EnviFlow.outputOf(e);
		var setup = CalculationSetup.of(sysQ).withSimulationRuns(1);
		var simulator = Simulator.create(setup, db);
		simulator.nextRun();

		var result = simulator.getResult();
		assertTrue(result.enviIndex().contains(ee));
		var eeVal = simulator.getResult().get(ee, 0);
		assertTrue(eeVal >= 21 && eeVal <= 63);

		db.delete(sysQ, sysP, procP, procQ);
	}

	@Test
	public void testFlowInTop() {

		// process P
		var procP = Process.of("P", p);
		db.insert(procP);
		var sysP = db.insert(ProductSystem.of(procP));

		// process Q <- P
		var procQ = Process.of("Q", q);
		procQ.input(p, 1.0);
		procQ.output(e, 42).uncertainty = Uncertainty.uniform(21, 63);
		db.insert(procQ);

		// system Q <- process Q <- system P
		var sysQ = ProductSystem.of(procQ);
		sysQ.link(TechFlow.of(sysP), procQ);
		db.insert(sysQ);

		var ee = EnviFlow.outputOf(e);
		var setup = CalculationSetup.of(sysQ).withSimulationRuns(1);
		var simulator = Simulator.create(setup, db);
		simulator.nextRun();

		var result = simulator.getResult();
		assertTrue(result.enviIndex().contains(ee));
		var eeVal = simulator.getResult().get(ee, 0);
		assertTrue(eeVal >= 21 && eeVal <= 63);

		db.delete(sysQ, sysP, procP, procQ);
	}
}
