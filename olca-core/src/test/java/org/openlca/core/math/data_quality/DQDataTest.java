package org.openlca.core.math.data_quality;

import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.results.FullResult;

public class DQDataTest {

	private final IDatabase db = Tests.getDb();
	private DQSystem dqSystem;
	private ProductSystem productSystem;
	private Process process1;
	private Process process2;
	private Flow elemFlow;
	private FlowProperty mass;

	@Before
	public void setup() {
		var units = db.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		mass = db.insert(FlowProperty.of("Mass", units));
		elemFlow = db.insert(Flow.elementary("CO2", mass));
		dqSystem = dqSystem();
		process1 = process("(1;2;3;4;5)", "(2;1;4;3;5)");
		process2 = process("(5;4;3;2;1)", "(4;5;2;3;1)");

		process1.input(process2.quantitativeReference.flow, 1);
		process1 = db.update(process1);

		productSystem = ProductSystem.of(process1);
		productSystem.link(process2, process1);
		productSystem = db.insert(productSystem);
	}

	@After
	public void shutdown() {
		db.clear();
	}

	private DQSystem dqSystem() {
		var sys = new DQSystem();
		for (int i = 1; i <= 5; i++) {
			var indicator = new DQIndicator();
			indicator.position = i;
			sys.indicators.add(indicator);
			for (int j = 1; j <= 5; j++) {
				var score = new DQScore();
				score.position = j;
				indicator.scores.add(score);
			}
		}
		return db.insert(sys);
	}

	private Process process(String processEntry, String flowEntry) {
		var product = db.insert(Flow.product("product", mass));
		var p = Process.of("process", product);
		p.dqSystem = dqSystem;
		p.dqEntry = processEntry;
		p.exchangeDqSystem = dqSystem;
		p.output(elemFlow, 1.0)
				.dqEntry = flowEntry;
		return db.insert(p);
	}

	@Test
	public void testGetData() {
		var setup = DQCalculationSetup.of(productSystem);
		var product1 = TechFlow.of(process1);
		var product2 = TechFlow.of(process2);

		var result = FullResult.of(db, productSystem);
		var iFlow = result.enviIndex().at(0);

		// test process data
		var dqData = DQResult.of(db, setup, result);
		assertArrayEquals(new int[]{1, 2, 3, 4, 5}, dqData.get(product1));
		assertArrayEquals(new int[]{5, 4, 3, 2, 1}, dqData.get(product2));

		// test exchange data
		assertArrayEquals(new int[]{2, 1, 4, 3, 5}, dqData.get(product1, iFlow));
		assertArrayEquals(new int[]{4, 5, 2, 3, 1}, dqData.get(product2, iFlow));
	}
}
