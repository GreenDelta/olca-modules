package org.openlca.core.math;

import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.TestSystem;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResultProvider;

public class CostTests {

	@Test
	public void test() {

		Process electricity = TestProcess
				.forOutput("Electricity", 1, "MJ")
				.addCosts("Electricity", 5, "EUR")
				.elemOut("CO2", 3, "kg")
				.get();

		Process wood = TestProcess
				.forOutput("Wood", 1, "kg")
				.addCosts("Wood", 1, "EUR")
				.get();

		Process chair = TestProcess
				.forOutput("Chair", 1, "piece")
				.addCosts("Chair", 25, "EUR")
				.prodIn("Electricity", 2, "MJ")
				.addCosts("Electricity", 10, "EUR")
				.prodIn("Wood", 5, "kg")
				.addCosts("Wood", 5, "EUR")
				.get();

		Process disposal = TestProcess
				.forOutput("Disposal of chair", 1, "piece")
				.addCosts("Disposal of chair", 2, "EUR")
				.get();

		Process usage = TestProcess
				.forOutput("Sitting", 10, "years")
				.addCosts("Sitting", 135, "EUR")
				.prodIn("Chair", 5, "piece")
				.addCosts("Chair", 125, "EUR")
				.prodIn("Disposal of chair", 5, "piece")
				.addCosts("Disposal of chair", 10, "EUR")
				.get();

		ProductSystem system = TestSystem
				.of(usage)
				.link(disposal)
				.link(chair)
				.link(wood)
				.link(electricity)
				.get();

		FullResultProvider result = TestSystem.calculate(system);
		System.out.println(result.getTotalCostResult());

	}

}
