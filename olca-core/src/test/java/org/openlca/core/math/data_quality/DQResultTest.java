package org.openlca.core.math.data_quality;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptor;

public class DQResultTest {

	private DQSystem dqSystem;
	private ProductSystem system;
	private Process process1;
	private Process process2;
	private Flow pFlow2;
	private Flow eFlow1;
	private Flow eFlow2;
	private ImpactMethod method;
	private ImpactCategory impact;

	@Before
	public void setup() {
		var units = Tests.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		var mass = Tests.insert(FlowProperty.of("Mass", units));

		var product1 = Tests.insert(Flow.product("product 1", mass));
		pFlow2 = Tests.insert(Flow.product("product 2", mass));
		eFlow1 = Tests.insert(Flow.elementary("elem 1", mass));
		eFlow2 = Tests.insert(Flow.elementary("elem 2", mass));

		createDQSystem();
		ProcessDao dao = new ProcessDao(Tests.getDb());
		process1 = process(product1);
		process1.quantitativeReference.dqEntry = "(1;2;3;4;5)";
		process1.input(pFlow2, 2);
		process1.input(eFlow1, 3).dqEntry = "(1;2;3;4;5)";
		process1.input(eFlow2, 4).dqEntry = "(5;4;3;2;1)";
		process1.dqEntry = "(1;2;3;4;5)";
		process1 = dao.insert(process1);

		process2 = process(pFlow2);
		process2.quantitativeReference.dqEntry = "(5;4;3;2;1)";
		process2.input(eFlow1, 5).dqEntry = "(5;4;3;2;1)";
		process2.input(eFlow2, 6).dqEntry = "(1;2;3;4;5)";
		process2.dqEntry = "(5;4;3;2;1)";
		process2 = dao.insert(process2);
		createProductSystem();
		createImpactMethod();
	}

	@After
	public void shutdown() {
		Tests.clearDb();
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
		system = ProductSystem.of(process1);
		system.targetAmount = 1;
		system.processes.add(process1.id);
		system.processes.add(process2.id);
		ProcessLink link = new ProcessLink();
		link.flowId = pFlow2.id;
		link.providerId = process2.id;
		for (Exchange e : process1.exchanges) {
			if (e.flow.id == pFlow2.id)
				link.exchangeId = e.id;
		}
		link.processId = process1.id;
		system.processLinks.add(link);
		system = Tests.insert(system);
	}

	private Process process(Flow product) {
		Process p = Process.of(product.name, product);
		p.dqSystem = dqSystem;
		p.exchangeDqSystem = dqSystem;
		return p;
	}

	private void createImpactMethod() {
		impact = new ImpactCategory();
		impact.factor(eFlow1, 2);
		impact.factor(eFlow2, 8);
		impact = Tests.insert(impact);
		method = new ImpactMethod();
		method.impactCategories.add(impact);
		method = Tests.insert(method);
	}

	@Test
	public void test() {
		var calculator = new SystemCalculator(
				Tests.getDb(),
				Tests.getDefaultSolver());
		var setup = new CalculationSetup(system);
		setup.setAmount(1);
		setup.impactMethod = Descriptor.of(method);
		var cResult = calculator.calculateContributions(setup);
		var dqSetup = DQCalculationSetup.of(system);
		var result = DQResultMap.calculate(Tests.getDb(), cResult, dqSetup);
		checkResults(result);
	}

	@Test
	public void test2() {
		var calculator = new SystemCalculator(
				Tests.getDb(),
				Tests.getDefaultSolver());
		var setup = new CalculationSetup(system);
		setup.setAmount(1);
		setup.impactMethod = Descriptor.of(method);
		var result = calculator.calculateContributions(setup);
		var dqSetup = DQCalculationSetup.of(system);
		var dqResult = DQResult.of(Tests.getDb(), dqSetup, result);

		assertArrayEquals(a(1, 2, 3, 4, 5), r(dqResult, process1));
		assertArrayEquals(a(5, 4, 3, 2, 1), r(dqResult, process2));
		assertArrayEquals(a(4, 4, 3, 2, 2), r(dqResult, eFlow1));
		assertArrayEquals(a(2, 3, 3, 4, 4), r(dqResult, eFlow2));
		assertArrayEquals(a(1, 2, 3, 4, 5), r(dqResult, process1, eFlow1));
		assertArrayEquals(a(5, 4, 3, 2, 1), r(dqResult, process2, eFlow1));
		assertArrayEquals(a(5, 4, 3, 2, 1), r(dqResult, process1, eFlow2));
		assertArrayEquals(a(1, 2, 3, 4, 5), r(dqResult, process2, eFlow2));

		assertArrayEquals(a(2, 3, 3, 3, 4), r(dqResult, impact));
		assertArrayEquals(a(4, 4, 3, 2, 2), r(dqResult, process1, impact));
		assertArrayEquals(a(2, 2, 3, 4, 4), r(dqResult, process2, impact));
	}

	private int[] r(DQResult dq, Flow flow) {
		var iflow = new IndexFlow();
		iflow.flow = Descriptor.of(flow);
		return dq.get(iflow);
	}

	private int[] r(DQResult dq, Process process) {
		var product = ProcessProduct.of(process);
		return dq.get(product);
	}

	private int[] r(DQResult dq, Process process, Flow flow) {
		var product = ProcessProduct.of(process);
		var iflow = new IndexFlow();
		iflow.flow = Descriptor.of(flow);
		return dq.get(product, iflow);
	}

	private int[] r(DQResult dq, ImpactCategory impact) {
		return dq.get(Descriptor.of(impact));
	}

	private int[] r(DQResult dq, Process process, ImpactCategory impact) {
		var product = ProcessProduct.of(process);
		return dq.get(Descriptor.of(impact), product);
	}

	private void checkResults(DQResultMap result) {
		assertArrayEquals(a(4, 4, 3, 2, 2), getResult(result, eFlow1));
		assertArrayEquals(a(2, 3, 3, 4, 4), getResult(result, eFlow2));
		assertArrayEquals(a(2, 3, 3, 3, 4), getResult(result, impact));
		assertArrayEquals(a(1, 2, 3, 4, 5), getResult(result, process1, eFlow1));
		assertArrayEquals(a(5, 4, 3, 2, 1), getResult(result, process2, eFlow1));
		assertArrayEquals(a(5, 4, 3, 2, 1), getResult(result, process1, eFlow2));
		assertArrayEquals(a(1, 2, 3, 4, 5), getResult(result, process2, eFlow2));
		assertArrayEquals(a(4, 4, 3, 2, 2), getResult(result, process1, impact));
		assertArrayEquals(a(2, 2, 3, 4, 4), getResult(result, process2, impact));
		assertArrayEquals(a(1, 2, 3, 4, 5), getResult(result, process1));
		assertArrayEquals(a(5, 4, 3, 2, 1), getResult(result, process2));
	}

	private int[] a(int... vals) {
		return vals;
	}

	private int[] getResult(DQResultMap result, Flow flow) {
		return result.get(Descriptor.of(flow));
	}

	private int[] getResult(DQResultMap result, Process process) {
		return result.get(Descriptor.of(process));
	}

	private int[] getResult(DQResultMap result, ImpactCategory impact) {
		return result.get(Descriptor.of(impact));
	}

	private int[] getResult(DQResultMap result, Process process, Flow flow) {
		return result.get(Descriptor.of(process),
				Descriptor.of(flow));
	}

	private int[] getResult(DQResultMap result, Process process,
							ImpactCategory impact) {
		return result.get(Descriptor.of(process),
				Descriptor.of(impact));
	}

}
