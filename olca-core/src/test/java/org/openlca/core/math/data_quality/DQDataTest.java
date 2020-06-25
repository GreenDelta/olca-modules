package org.openlca.core.math.data_quality;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.results.ContributionResult;

public class DQDataTest {

	private DQSystem dqSystem;
	private ProductSystem productSystem;
	private Process process1;
	private Process process2;
	private Flow elemFlow;
	private FlowProperty mass;

	@Before
	public void setup() {
		var units = Tests.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		mass = Tests.insert(FlowProperty.of("Mass", units));
		elemFlow = Tests.insert(Flow.elementary("CO2", mass));
		dqSystem = dqSystem();
		process1 = process("(1;2;3;4;5)", "(2;1;4;3;5)");
		process2 = process("(5;4;3;2;1)", "(4;5;2;3;1)");
		productSystem = ProductSystem.of(process1);
		productSystem.processes.add(process2.id);
		productSystem = Tests.insert(productSystem);
	}

	@After
	public void shutdown() {
		Tests.clearDb();
	}

	private DQSystem dqSystem() {
		var sys = new DQSystem();
		for (int i = 1; i <= 5; i++) {
			DQIndicator indicator = new DQIndicator();
			indicator.position = i;
			sys.indicators.add(indicator);
			for (int j = 1; j <= 5; j++) {
				DQScore score = new DQScore();
				score.position = j;
				indicator.scores.add(score);
			}
		}
		return Tests.insert(sys);
	}

	private Process process(String processEntry, String flowEntry) {
		var product = Tests.insert(Flow.product("product", mass));
		var p = Process.of("process", product);
		p.dqSystem = dqSystem;
		p.dqEntry = processEntry;
		p.exchangeDqSystem = dqSystem;
		p.output(elemFlow, 1.0)
				.dqEntry = flowEntry;
		return Tests.insert(p);
	}

	@Test
	public void test() {
		var setup = DQCalculationSetup.of(productSystem);
		var data = DQData.load(
				Tests.getDb(), setup, new long[]{elemFlow.id});
		assertEquals(dqSystem.id, setup.processSystem.id);
		assertEquals(dqSystem.id, setup.exchangeSystem.id);
		assertArrayEquals(new double[]{1, 2, 3, 4, 5},
				data.processData.get(process1.id), 0);
		assertArrayEquals(new double[]{5, 4, 3, 2, 1},
				data.processData.get(process2.id), 0);
		assertArrayEquals(new double[]{2, 1, 4, 3, 5},
				data.exchangeData.get(LongPair.of(process1.id, elemFlow.id)), 0);
		assertArrayEquals(new double[]{4, 5, 2, 3, 1},
				data.exchangeData.get(LongPair.of(process2.id, elemFlow.id)), 0);
	}

	@Test
	public void testLoadData() {
		var setup = DQCalculationSetup.of(productSystem);
		var product1 = ProcessProduct.of(process1);
		var product2 = ProcessProduct.of(process2);

		var result = new ContributionResult();
		result.techIndex = new TechIndex(product1);
		result.techIndex.put(product2);
		result.flowIndex = FlowIndex.create();
		result.flowIndex.putOutput(Descriptor.of(elemFlow));
		var iFlow = result.flowIndex.at(0);

		// test process data
		var dqData = DQResult.of(Tests.getDb(), setup, result);
		assertArrayEquals(new int[]{1, 2, 3, 4, 5}, dqData.get(product1));
		assertArrayEquals(new int[]{5, 4, 3, 2, 1}, dqData.get(product2));

		// test exchange data
		assertArrayEquals(new int[]{2, 1, 4, 3, 5}, dqData.get(product1, iFlow));
		assertArrayEquals(new int[]{4, 5, 2, 3, 1}, dqData.get(product2, iFlow));
	}
}
