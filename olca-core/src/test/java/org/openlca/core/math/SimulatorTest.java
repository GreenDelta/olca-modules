package org.openlca.core.math;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UnitGroup;

public class SimulatorTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testImpactParam() {

		// create a simple model with an uncertain parameter in a LCIA category
		var units = db.insert(UnitGroup.of("Mass units", "kg"));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var ch4 = db.insert(Flow.elementary("CH4", mass));
		var p = db.insert(Flow.product("p", mass));

		var process = Process.of("P", p);
		process.output(ch4, 1);
		db.insert(process);
		var system = db.insert(ProductSystem.of(process));

		var gwp = ImpactCategory.of("GWP");
		var factor = gwp.factor(ch4, 0);
		factor.formula = "1 * param";
		var param = Parameter.impact("param", 0);
		param.uncertainty = Uncertainty.uniform(22, 26);
		gwp.parameters.add(param);
		db.insert(gwp);
		var method = ImpactMethod.of("mathod");
		method.impactCategories.add(gwp);
		db.insert(method);

		// create the simulator
		var setup = CalculationSetup.of(system)
				.withSimulationRuns(100)
				.withImpactMethod(method);
		var simulator = Simulator.create(setup, db)
				.withSolver(new JavaSolver());

		// check the simulation results
		for (int i = 0; i < 100; i++) {
			var r = simulator.nextRun();
			double[] impacts = r.totalImpactResults();
			Assert.assertEquals(1, impacts.length);
			double val = impacts[0];
			Assert.assertTrue(val >= 22 && val <= 26);
		}

		db.delete(method, gwp, system, process, p, ch4, mass, units);
	}

}
