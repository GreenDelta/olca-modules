package org.openlca.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.solvers.Factorization;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UnitGroup;

/// Checks that the simulator disposes the results of sub-systems, and with
/// this their factorizations, which can hold native memory.
public class SimulatorDisposeTest {

	private final IDatabase db = Tests.getDb();
	private UnitGroup units;
	private FlowProperty mass;
	private Flow e, p, q;
	private Process procP, procQ;
	private ProductSystem sysP, sysQ;

	@Before
	public void setup() {
		units = db.insert(UnitGroup.of("Units of mass", "kg"));
		mass = db.insert(FlowProperty.of("Mass", units));
		e = db.insert(Flow.elementary("e", mass));
		p = db.insert(Flow.product("p", mass));
		q = db.insert(Flow.product("q", mass));

		procP = Process.of("P", p);
		procP.output(e, 42).uncertainty = Uncertainty.uniform(21, 63);
		db.insert(procP);
		procQ = Process.of("Q", q);
		procQ.input(p, 1.0);
		db.insert(procQ);

		// system Q <- process Q <- system P <- process P
		sysP = db.insert(ProductSystem.of(procP));
		sysQ = ProductSystem.of(procQ);
		sysQ.link(TechFlow.of(sysP), procQ);
		db.insert(sysQ);
	}

	@After
	public void cleanup() {
		db.delete(sysQ, sysP, procQ, procP, q, p, e, mass, units);
	}

	@Test
	public void testDisposeSubResults() {
		var solver = new TrackingSolver();
		var setup = CalculationSetup.of(sysQ).withSimulationRuns(10);
		var simulator = Simulator.create(setup, db).withSolver(solver);

		for (int i = 0; i < 10; i++) {
			var r = simulator.nextRun();
			assertNotNull(r);
			r.dispose();
			// only the factorization of the last sub-system result is alive
			assertEquals(1, solver.alive());
		}
		assertEquals(20, solver.factorizations.size());

		simulator.dispose();
		assertEquals(0, solver.alive());
		simulator.dispose(); // no effect
		assertEquals(0, solver.alive());
	}

	private static class TrackingSolver extends JavaSolver {

		final List<Factorization> factorizations = new ArrayList<>();

		@Override
		public Factorization factorize(MatrixReader matrix) {
			var f = super.factorize(matrix);
			factorizations.add(f);
			return f;
		}

		long alive() {
			return factorizations.stream()
				.filter(f -> !f.isDisposed())
				.count();
		}
	}
}
