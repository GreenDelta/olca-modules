package org.openlca.core.math.data_quality;

import java.math.RoundingMode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
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
import org.openlca.core.model.FlowPropertyFactor;
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
		createUnitGroup();
		createProperty();
		pFlow1 = createFlow(FlowType.PRODUCT_FLOW);
		pFlow2 = createFlow(FlowType.PRODUCT_FLOW);
		eFlow1 = createFlow(FlowType.ELEMENTARY_FLOW);
		eFlow2 = createFlow(FlowType.ELEMENTARY_FLOW);
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
		IDatabase db = Tests.getDb();
		new ImpactMethodDao(db).delete(method);
		method.impactCategories
				.forEach(i -> new ImpactCategoryDao(db).delete(i));
		new ProductSystemDao(db).delete(system);
		new ProcessDao(db).delete(process1);
		new ProcessDao(db).delete(process2);
		new DQSystemDao(db).delete(dqSystem);
		new FlowDao(db).delete(pFlow1);
		new FlowDao(db).delete(pFlow2);
		new FlowDao(db).delete(eFlow1);
		new FlowDao(db).delete(eFlow2);
		new FlowPropertyDao(db).delete(property);
		new UnitGroupDao(db).delete(unitGroup);
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
		Exchange e = p.exchange(flow);
		e.dqEntry = dqEntry;
		e.isInput = input;
		e.amount = amount;
		return e;
	}

	private Flow createFlow(FlowType type) {
		Flow flow = new Flow();
		flow.flowType = type;
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.conversionFactor = 1d;
		factor.flowProperty = property;
		flow.flowPropertyFactors.add(factor);
		flow.referenceFlowProperty = property;
		return new FlowDao(Tests.getDb()).insert(flow);
	}

	private void createUnitGroup() {
		unitGroup = new UnitGroup();
		Unit unit = new Unit();
		unit.name = "unit";
		unit.conversionFactor = 1;
		unitGroup.units.add(unit);
		unitGroup.referenceUnit = unit;
		unitGroup = new UnitGroupDao(Tests.getDb()).insert(unitGroup);
	}

	private void createProperty() {
		property = new FlowProperty();
		property.unitGroup = unitGroup;
		property = new FlowPropertyDao(Tests.getDb()).insert(property);
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
