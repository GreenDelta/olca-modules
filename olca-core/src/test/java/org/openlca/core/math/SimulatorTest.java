package org.openlca.core.math;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestData;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.Tests;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.results.SimpleResult;

public class SimulatorTest {

	@Test
	public void testImpactParam() {

		// create a simple model with an uncertain
		// parameter in a LCIA category
		Process p = TestProcess.refProduct("p", 1.0, "kg")
				.elemOut("CH4", 1.0, "kg")
				.get();
		ProductSystem s = TestSystem.of(p).get();
		ImpactMethod m = TestData.method("method",
				TestData.impact("GWP")
						.factor("CH4", "1 * param", "kg")
						.parameter("param", Uncertainty.uniform(22, 26))
						.get());

		// create the simulator
		CalculationSetup setup = new CalculationSetup(s);
		setup.withUncertainties = true;
		setup.impactMethod = Descriptor.of(m);
		Simulator simulator = Simulator.create(
				setup, Tests.getDb(), new JavaSolver());

		// check the simulation results
		for (int i = 0; i < 100; i++) {
			SimpleResult r = simulator.nextRun();
			double[] impacts = r.totalImpactResults;
			Assert.assertEquals(1, impacts.length);
			double val = impacts[0];
			Assert.assertTrue(val >= 22 && val <= 26);
		}

		Arrays.asList(s, m, p).forEach(Tests::delete);
	}

}
