package org.openlca.core.math.data_quality;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class DirectCalculationTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSimpleProcess() {

		// reference data
		var units = Tests.insert(
				UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = Tests.insert(
				FlowProperty.of("Mass", units));
		var steel = Tests.insert(
				Flow.product("steel", mass));
		var co2 = Tests.insert(
				Flow.elementary("CO2", mass));

		// process
		var process = Process.of("process", steel);
		process.output(co2, 2).dqEntry = "(1;2;3;4;5)";
		process.exchangeDqSystem = dqSystem();
		process = Tests.insert(process);

		// calculation
		var system = ProductSystem.of(process);
		system.withoutNetwork = true;
		var setup = new CalculationSetup(system);
		var calculator = new SystemCalculator(
				Tests.getDb(),
				Tests.getDefaultSolver());
		var result = calculator.calculateFull(setup);
		var dqSetup = DQCalculationSetup.of((system));
		var dqResult = DQResult.of(db, dqSetup, result);

		// check the result
		var dq = dqResult.get(result.flowIndex.at(0));
		Assert.assertArrayEquals(new int[]{1,2,3,4,5}, dq);

		Tests.clearDb();
	}

	private DQSystem dqSystem() {
		var system = new DQSystem();
		for (int i = 1; i <= 5; i++) {
			var indicator = new DQIndicator();
			indicator.position = i;
			system.indicators.add(indicator);
			for (int j = 1; j <= 5; j++) {
				var score = new DQScore();
				score.position = j;
				indicator.scores.add(score);
			}
		}
		return Tests.insert(system);
	}

}
