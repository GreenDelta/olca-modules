package org.openlca.core.math.data_quality;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class DirectCalculationTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testSimpleProcess() {

		// reference data
		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = db.insert(FlowProperty.of("Mass", units));
		var steel = db.insert(Flow.product("steel", mass));
		var co2 = db.insert(Flow.elementary("CO2", mass));

		// process
		var process = Process.of("process", steel);
		process.output(co2, 2).dqEntry = "(1;2;3;4;5)";
		process.exchangeDqSystem = dqSystem();
		process = db.insert(process);

		// calculation
		var setup = CalculationSetup.fullAnalysis(process);
		var calculator = new SystemCalculator(Tests.getDb());
		var result = calculator.calculateFull(setup);
		var dqSetup = DQCalculationSetup.of(setup);
		var dqResult = DQResult.of(db, dqSetup, result);

		// check the result; note that there could be some
		// artifact flows from other tests in the result;
		// this we first find the entry for CO2 in the
		// result index
		EnviFlow co2IdxFlow = null;
		for (var f : result.enviIndex()) {
			if (f.flow().id == co2.id) {
				co2IdxFlow = f;
				break;
			}
		}
		var dq = dqResult.get(co2IdxFlow);
		Assert.assertArrayEquals(new int[]{1, 2, 3, 4, 5}, dq);

		// delete artifacts
		db.delete(process, process.exchangeDqSystem, co2, steel, mass, units);
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
		return db.insert(system);
	}
}
