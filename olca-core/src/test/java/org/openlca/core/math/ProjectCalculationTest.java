package org.openlca.core.math;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.Tests;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.Process;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.results.ProjectResult;
import org.openlca.core.results.ResultItemView;

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

		var result = ProjectResult.calculate(project, Tests.getDb());
		var items = ResultItemView.of(result);
		assertEquals(2, items.enviFlows().size());

		AtomicInteger icount = new AtomicInteger(0);
		for (EnviFlow f : items.enviFlows()) {
			result.getContributions(f).forEach(item -> {
				icount.incrementAndGet();
				switch (f.flow().name) {
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
