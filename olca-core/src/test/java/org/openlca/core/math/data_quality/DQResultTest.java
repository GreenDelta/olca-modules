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
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.cache.MatrixCache;
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
		process1 = process(
				exchange(1, "(1;2;3;4;5)", pFlow1, false),
				exchange(2, null, pFlow2, true),
				exchange(3, "(1;2;3;4;5)", eFlow1, true),
				exchange(4, "(5;4;3;2;1)", eFlow2, true));
		process2 = process(
				exchange(1, "(5;4;3;2;1)", pFlow2, false),
				exchange(5, "(5;4;3;2;1)", eFlow1, true),
				exchange(6, "(1;2;3;4;5)", eFlow2, true));
		createProductSystem();
		createImpactMethod();
	}

	@After
	public void shutdown() {
		new ImpactMethodDao(Tests.getDb()).delete(method);
		new ProductSystemDao(Tests.getDb()).delete(system);
		new ProcessDao(Tests.getDb()).delete(process1);
		new ProcessDao(Tests.getDb()).delete(process2);
		new DQSystemDao(Tests.getDb()).delete(dqSystem);
		new FlowDao(Tests.getDb()).delete(pFlow1);
		new FlowDao(Tests.getDb()).delete(pFlow2);
		new FlowDao(Tests.getDb()).delete(eFlow1);
		new FlowDao(Tests.getDb()).delete(eFlow2);
		new FlowPropertyDao(Tests.getDb()).delete(property);
		new UnitGroupDao(Tests.getDb()).delete(unitGroup);
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
		system.getProcesses().add(process1.getId());
		system.getProcesses().add(process2.getId());
		ProcessLink link = new ProcessLink();
		link.flowId = pFlow2.getId();
		link.providerId = process2.getId();
		for (Exchange e : process1.getExchanges()) {
			if (e.flow.getId() == pFlow2.getId())
				link.exchangeId = e.getId();
		}
		link.processId = process1.getId();
		system.getProcessLinks().add(link);
		system.setReferenceProcess(process1);
		system.setReferenceExchange(process1.getQuantitativeReference());
		system.setTargetAmount(1);
		system.setTargetFlowPropertyFactor(pFlow1.getReferenceFactor());
		system.setTargetUnit(unitGroup.getReferenceUnit());
		system = new ProductSystemDao(Tests.getDb()).insert(system);
	}

	/** The first exchange is the reference product. */
	private Process process(Exchange... exchanges) {
		Process p = new Process();
		p.dqSystem = dqSystem;
		p.dqEntry = exchanges[0].dqEntry;
		p.exchangeDqSystem = dqSystem;
		p.setQuantitativeReference(exchanges[0]);
		for (Exchange e : exchanges)
			p.getExchanges().add(e);
		return new ProcessDao(Tests.getDb()).insert(p);
	}

	private Exchange exchange(double amount, String dqEntry, Flow flow, boolean input) {
		Exchange e = new Exchange();
		e.dqEntry = dqEntry;
		final Flow flow1 = flow;
		e.flow = flow1;
		e.isInput = input;
		e.flowPropertyFactor = flow.getReferenceFactor();
		e.unit = unitGroup.getReferenceUnit();
		e.amount = amount;
		return e;
	}

	private Flow createFlow(FlowType type) {
		Flow flow = new Flow();
		flow.setFlowType(type);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setConversionFactor(1d);
		factor.setFlowProperty(property);
		flow.getFlowPropertyFactors().add(factor);
		flow.setReferenceFlowProperty(property);
		return new FlowDao(Tests.getDb()).insert(flow);
	}

	private void createUnitGroup() {
		unitGroup = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		unit.setConversionFactor(1);
		unitGroup.getUnits().add(unit);
		unitGroup.setReferenceUnit(unit);
		unitGroup = new UnitGroupDao(Tests.getDb()).insert(unitGroup);
	}

	private void createProperty() {
		property = new FlowProperty();
		property.setUnitGroup(unitGroup);
		property = new FlowPropertyDao(Tests.getDb()).insert(property);
	}

	private void createImpactMethod() {
		method = new ImpactMethod();
		ImpactCategory c = new ImpactCategory();
		c.impactFactors.add(createFactor(2, eFlow1));
		c.impactFactors.add(createFactor(8, eFlow2));
		method.impactCategories.add(c);
		method = new ImpactMethodDao(Tests.getDb()).insert(method);
	}

	private ImpactFactor createFactor(double factor, Flow flow) {
		ImpactFactor f = new ImpactFactor();
		f.value = factor;
		f.flow = flow;
		f.flowPropertyFactor = flow.getReferenceFactor();
		f.unit = unitGroup.getReferenceUnit();
		return f;
	}

	@Test
	public void test() {
		SystemCalculator calculator = new SystemCalculator(
				MatrixCache.createEager(Tests.getDb()),
				Tests.getDefaultSolver());
		CalculationSetup setup = new CalculationSetup(system);
		setup.setAmount(1);
		setup.impactMethod = Descriptors.toDescriptor(method);
		ContributionResult cResult = calculator.calculateContributions(setup);
		DQCalculationSetup dqSetup = new DQCalculationSetup();
		dqSetup.productSystemId = system.getId();
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
		Assert.assertArrayEquals(a(4, 4, 3, 2, 2), getResult(result, eFlow1), 0.5);
		Assert.assertArrayEquals(a(2, 3, 3, 4, 4), getResult(result, eFlow2), 0.5);
		Assert.assertArrayEquals(a(2, 3, 3, 3, 4), getResult(result, impact), 0.5);
		Assert.assertArrayEquals(a(1, 2, 3, 4, 5), getResult(result, process1, eFlow1), 0.5);
		Assert.assertArrayEquals(a(5, 4, 3, 2, 1), getResult(result, process2, eFlow1), 0.5);
		Assert.assertArrayEquals(a(5, 4, 3, 2, 1), getResult(result, process1, eFlow2), 0.5);
		Assert.assertArrayEquals(a(1, 2, 3, 4, 5), getResult(result, process2, eFlow2), 0.5);
		Assert.assertArrayEquals(a(4, 4, 3, 2, 2), getResult(result, process1, impact), 0.5);
		Assert.assertArrayEquals(a(2, 2, 3, 4, 4), getResult(result, process2, impact), 0.5);
		Assert.assertArrayEquals(a(1, 2, 3, 4, 5), getResult(result, process1), 0.5);
		Assert.assertArrayEquals(a(5, 4, 3, 2, 1), getResult(result, process2), 0.5);
	}

	private double[] a(double... vals) {
		return vals;
	}

	private double[] getResult(DQResult result, Flow flow) {
		return result.get(Descriptors.toDescriptor(flow));
	}

	private double[] getResult(DQResult result, Process process) {
		return result.get(Descriptors.toDescriptor(process));
	}

	private double[] getResult(DQResult result, ImpactCategory impact) {
		return result.get(Descriptors.toDescriptor(impact));
	}

	private double[] getResult(DQResult result, Process process, Flow flow) {
		return result.get(Descriptors.toDescriptor(process), Descriptors.toDescriptor(flow));
	}

	private double[] getResult(DQResult result, Process process, ImpactCategory impact) {
		return result.get(Descriptors.toDescriptor(process), Descriptors.toDescriptor(impact));
	}

}
