package org.openlca.core.math.data_quality;

import java.math.RoundingMode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.results.ContributionResult;

public class DQResultTest {

	private DQSystem dqSystem;
	private ProductSystem system;
	private Process process1;
	private Process process2;
	private Flow pFlow1;
	private Flow pFlow2;
	private Flow eFlow1;
	private Flow eFlow2;
	private FlowProperty property;
	private UnitGroup unitGroup;
	private ImpactMethod method;

	@Before
	public void setup() {
		unitGroup = Tests.insert(UnitGroup.of("Mass units", Unit.of("kg")));
		property = Tests.insert(FlowProperty.of("Mass", unitGroup));

		pFlow1 = Tests.insert(Flow.of("product 1", FlowType.PRODUCT_FLOW, property));
		pFlow2 = Tests.insert(Flow.of("product 2", FlowType.PRODUCT_FLOW, property));
		eFlow1 = Tests.insert(Flow.of("elem 1", FlowType.ELEMENTARY_FLOW, property));
		eFlow2 = Tests.insert(Flow.of("elem 2", FlowType.ELEMENTARY_FLOW, property));

		createDQSystem();
		ProcessDao dao = new ProcessDao(Tests.getDb());
		process1 = process();
		Exchange ref1 = exchange(process1, 1, "(1;2;3;4;5)", pFlow1, false);
		exchange(process1, 2, null, pFlow2, true);
		exchange(process1, 3, "(1;2;3;4;5)", eFlow1, true);
		exchange(process1, 4, "(5;4;3;2;1)", eFlow2, true);
		process1.dqEntry = ref1.dqEntry;
		process1.quantitativeReference = ref1;
		process1 = dao.insert(process1);
		process2 = process();
		Exchange ref2 = exchange(process2, 1, "(5;4;3;2;1)", pFlow2, false);
		exchange(process2, 5, "(5;4;3;2;1)", eFlow1, true);
		exchange(process2, 6, "(1;2;3;4;5)", eFlow2, true);
		process2.dqEntry = ref2.dqEntry;
		process2.quantitativeReference = ref2;
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
		system = new ProductSystem();
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
		system.referenceProcess = process1;
		system.referenceExchange = process1.quantitativeReference;
		system.targetAmount = 1;
		system.targetFlowPropertyFactor = pFlow1.getReferenceFactor();
		system.targetUnit = unitGroup.referenceUnit;
		system = new ProductSystemDao(Tests.getDb()).insert(system);
	}

	/** The first exchange is the reference product. */
	private Process process() {
		Process p = new Process();
		p.dqSystem = dqSystem;
		p.exchangeDqSystem = dqSystem;
		return p;
	}

	private Exchange exchange(Process p, double amount, String dqEntry,
			Flow flow, boolean input) {
		Exchange e = input
				? p.input(flow, amount)
				: p.output(flow, amount);
		e.dqEntry = dqEntry;
		return e;
	}

	private void createImpactMethod() {
		ImpactCategory c = new ImpactCategory();
		c.impactFactors.add(createFactor(2, eFlow1));
		c.impactFactors.add(createFactor(8, eFlow2));
		c = new ImpactCategoryDao(Tests.getDb()).insert(c);
		method = new ImpactMethod();
		method.impactCategories.add(c);
		method = new ImpactMethodDao(Tests.getDb()).insert(method);
	}

	private ImpactFactor createFactor(double factor, Flow flow) {
		ImpactFactor f = new ImpactFactor();
		f.value = factor;
		f.flow = flow;
		f.flowPropertyFactor = flow.getReferenceFactor();
		f.unit = unitGroup.referenceUnit;
		return f;
	}

	@Test
	public void test() {
		SystemCalculator calculator = new SystemCalculator(
				Tests.getDb(),
				Tests.getDefaultSolver());
		CalculationSetup setup = new CalculationSetup(system);
		setup.setAmount(1);
		setup.impactMethod = Descriptors.toDescriptor(method);
		ContributionResult cResult = calculator.calculateContributions(setup);
		DQCalculationSetup dqSetup = new DQCalculationSetup();
		dqSetup.productSystemId = system.id;
		dqSetup.aggregationType = AggregationType.WEIGHTED_AVERAGE;
		dqSetup.roundingMode = RoundingMode.HALF_UP;
		dqSetup.processingType = ProcessingType.EXCLUDE;
		dqSetup.exchangeDqSystem = dqSystem;
		dqSetup.processDqSystem = dqSystem;
		DQResult result = DQResult.calculate(Tests.getDb(), cResult, dqSetup);
		ImpactCategory impact = method.impactCategories.get(0);
		checkResults(result, impact);
	}

	private void checkResults(DQResult result, ImpactCategory impact) {
		Assert.assertArrayEquals(a(4, 4, 3, 2, 2), getResult(result, eFlow1));
		Assert.assertArrayEquals(a(2, 3, 3, 4, 4), getResult(result, eFlow2));
		Assert.assertArrayEquals(a(2, 3, 3, 3, 4), getResult(result, impact));
		Assert.assertArrayEquals(a(1, 2, 3, 4, 5),
				getResult(result, process1, eFlow1));
		Assert.assertArrayEquals(a(5, 4, 3, 2, 1),
				getResult(result, process2, eFlow1));
		Assert.assertArrayEquals(a(5, 4, 3, 2, 1),
				getResult(result, process1, eFlow2));
		Assert.assertArrayEquals(a(1, 2, 3, 4, 5),
				getResult(result, process2, eFlow2));
		Assert.assertArrayEquals(a(4, 4, 3, 2, 2),
				getResult(result, process1, impact));
		Assert.assertArrayEquals(a(2, 2, 3, 4, 4),
				getResult(result, process2, impact));
		Assert.assertArrayEquals(a(1, 2, 3, 4, 5), getResult(result, process1));
		Assert.assertArrayEquals(a(5, 4, 3, 2, 1), getResult(result, process2));
	}

	private int[] a(int... vals) {
		return vals;
	}

	private int[] getResult(DQResult result, Flow flow) {
		return result.get(Descriptors.toDescriptor(flow));
	}

	private int[] getResult(DQResult result, Process process) {
		return result.get(Descriptors.toDescriptor(process));
	}

	private int[] getResult(DQResult result, ImpactCategory impact) {
		return result.get(Descriptors.toDescriptor(impact));
	}

	private int[] getResult(DQResult result, Process process, Flow flow) {
		return result.get(Descriptors.toDescriptor(process),
				Descriptors.toDescriptor(flow));
	}

	private int[] getResult(DQResult result, Process process,
			ImpactCategory impact) {
		return result.get(Descriptors.toDescriptor(process),
				Descriptors.toDescriptor(impact));
	}

}
