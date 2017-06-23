package org.openlca.core.math.data_quality;

import java.math.RoundingMode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

public class DQDataTest {

	private DQSystem dqSystem;
	private ProductSystem pSystem;
	private Process process1;
	private Process process2;
	private Flow flow;

	@Before
	public void setup() {
		createFlow();
		createDQSystem();
		createProductSystem();
	}

	private void createDQSystem() {
		dqSystem = new DQSystem();
		for (int i = 1; i <= 5; i++) {
			DQIndicator indicator = new DQIndicator();
			indicator.position = i;
			dqSystem.indicators.add(indicator);
			for (int j = 1; j <= 5; j++) {
				DQScore score = new DQScore();
				score.position = j;
				indicator.scores.add(score);
			}
		}
		dqSystem = new DQSystemDao(Tests.getDb()).insert(dqSystem);
	}

	private void createProductSystem() {
		process1 = new ProcessDao(Tests.getDb()).insert(createProcess("(1;2;3;4;5)", "(2;1;4;3;5)"));
		process2 = new ProcessDao(Tests.getDb()).insert(createProcess("(5;4;3;2;1)", "(4;5;2;3;1)"));
		pSystem = new ProductSystem();
		pSystem.getProcesses().add(process1.getId());
		pSystem.getProcesses().add(process2.getId());
		pSystem = new ProductSystemDao(Tests.getDb()).insert(pSystem);
	}

	private Process createProcess(String dqEntry1, String dqEntry2) {
		Process process = new Process();
		process.dqSystem = dqSystem;
		process.dqEntry = dqEntry1;
		process.exchangeDqSystem = dqSystem;
		Exchange exchange = new Exchange();
		exchange.dqEntry = dqEntry2;
		final Flow flow1 = flow;
		exchange.flow = flow1;
		process.getExchanges().add(exchange);
		return process;
	}

	private void createFlow() {
		flow = new Flow();
		flow = new FlowDao(Tests.getDb()).insert(flow);
	}

	@Test
	public void test() {
		DQCalculationSetup setup = new DQCalculationSetup();
		setup.productSystemId = pSystem.getId();
		setup.aggregationType = AggregationType.WEIGHTED_AVERAGE;
		setup.roundingMode = RoundingMode.HALF_UP;
		setup.processingType = ProcessingType.EXCLUDE;
		setup.exchangeDqSystem = dqSystem;
		setup.processDqSystem = dqSystem;
		DQData data = DQData.load(Tests.getDb(), setup, new long[] { flow.getId() });
		Assert.assertEquals(dqSystem.getId(), setup.processDqSystem.getId());
		Assert.assertEquals(dqSystem.getId(), setup.exchangeDqSystem.getId());
		Assert.assertArrayEquals(new double[] { 1, 2, 3, 4, 5 }, data.processData.get(process1.getId()), 0);
		Assert.assertArrayEquals(new double[] { 5, 4, 3, 2, 1 }, data.processData.get(process2.getId()), 0);
		Assert.assertArrayEquals(new double[] { 2, 1, 4, 3, 5 },
				data.exchangeData.get(new LongPair(process1.getId(), flow.getId())), 0);
		Assert.assertArrayEquals(new double[] { 4, 5, 2, 3, 1 },
				data.exchangeData.get(new LongPair(process2.getId(), flow.getId())), 0);
	}

	@After
	public void shutdown() {
		new ProductSystemDao(Tests.getDb()).delete(pSystem);
		new ProcessDao(Tests.getDb()).delete(process1);
		new ProcessDao(Tests.getDb()).delete(process2);
		new DQSystemDao(Tests.getDb()).delete(dqSystem);
		new FlowDao(Tests.getDb()).delete(flow);
	}

}
