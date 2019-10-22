package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.Tests;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.model.Process;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.results.ProjectResult;

public class ProjectCalculationTest {

	@Test
	public void testScaleVariantByAmount() {
		Project project = new Project();
		Process p = TestProcess
				.refProduct("p", 1, "kg")
				.elemOut("e1", 0.5, "kg")
				.elemOut("e2", 2.0, "kg")
				.get();
		ProjectVariant var1 = new ProjectVariant();
		var1.productSystem = TestSystem.of(p).get();
		var1.amount = 2.0;
		project.variants.add(var1);

		Process w = TestProcess
				.refWaste("w", 1, "kg")
				.elemOut("e1", 0.5, "kg")
				.elemOut("e2", 2.0, "kg")
				.get();
		ProjectVariant var2 = new ProjectVariant();
		var2.productSystem = TestSystem.of(w).get();
		var2.amount = 2.0;
		project.variants.add(var2);

		SystemCalculator calc = new SystemCalculator(
				MatrixCache.createLazy(Tests.getDb()),
				new JavaSolver());
		ProjectResult r = calc.calculate(project);
		Set<FlowDescriptor> flows = r.getFlows();
		assertEquals(2, flows.size());

		AtomicInteger icount = new AtomicInteger(0);
		for (FlowDescriptor flow : flows) {
			r.getContributions(flow).contributions.forEach(item -> {
				icount.incrementAndGet();
				switch (flow.name) {
				case "e1":
					assertEquals(1.0, item.amount, 1e-10);
					break;
				case "e2":
					assertEquals(4.0, item.amount, 1e-10);
					break;
				}
			});
		}
		assertEquals(4, icount.get());
	}
}
