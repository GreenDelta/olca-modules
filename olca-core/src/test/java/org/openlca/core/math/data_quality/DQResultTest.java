package org.openlca.core.math.data_quality;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
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
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.results.ContributionResult;

public class DQResultTest {
	private DQSystem dqSystem;
	private ProductSystem pSystem;
	private Process process1;
	private Process process2;
	private Flow pFlow1;
	private Flow pFlow2;
	private Flow eFlow;
	private FlowProperty property;
	private UnitGroup unitGroup;

	@Before
	public void setup() {
		createUnitGroup();
		createProperty();
		pFlow1 = createFlow(FlowType.PRODUCT_FLOW);
		pFlow2 = createFlow(FlowType.PRODUCT_FLOW);
		eFlow = createFlow(FlowType.ELEMENTARY_FLOW);
		createDQSystem();
		process1 = createProcess(pFlow1, "(1;2;3;4;5)", "(2;1;4;3;5)", pFlow2);
		process2 = createProcess(pFlow2, "(5;4;3;2;1)", "(4;5;2;3;1)", null);
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
		pSystem = new ProductSystem();
		pSystem.getProcesses().add(process1.getId());
		pSystem.getProcesses().add(process2.getId());
		ProcessLink link = new ProcessLink();
		link.setFlowId(pFlow2.getId());
		link.setProviderId(process2.getId());
		link.setRecipientId(process1.getId());
		pSystem.getProcessLinks().add(link);
		pSystem.setReferenceProcess(process1);
		pSystem.setReferenceExchange(process1.getQuantitativeReference());
		pSystem.setTargetAmount(1);
		pSystem.setTargetFlowPropertyFactor(pFlow1.getReferenceFactor());
		pSystem.setTargetUnit(unitGroup.getReferenceUnit());
		pSystem = new ProductSystemDao(Tests.getDb()).insert(pSystem);
	}

	private Process createProcess(Flow pFlow, String dqEntry1, String dqEntry2, Flow inputFlow) {
		Process process = new Process();
		process.dqSystem = dqSystem;
		process.dqEntry = dqEntry1;
		process.exchangeDqSystem = dqSystem;
		Exchange product = new Exchange();
		product.setFlow(pFlow);
		product.setFlowPropertyFactor(pFlow.getReferenceFactor());
		product.setUnit(unitGroup.getReferenceUnit());
		product.setAmountValue(1d);
		process.getExchanges().add(product);
		process.setQuantitativeReference(product);
		Exchange elem = new Exchange();
		elem.setDqEntry(dqEntry2);
		elem.setFlow(eFlow);
		elem.setFlowPropertyFactor(eFlow.getReferenceFactor());
		elem.setUnit(unitGroup.getReferenceUnit());
		elem.setAmountValue(3d);
		process.getExchanges().add(elem);
		if (inputFlow == null)
			return new ProcessDao(Tests.getDb()).insert(process);
		Exchange input = new Exchange();
		input.setAmountValue(2d);
		input.setFlow(inputFlow);
		input.setFlowPropertyFactor(inputFlow.getReferenceFactor());
		input.setUnit(unitGroup.getReferenceUnit());
		input.setInput(true);
		process.getExchanges().add(input);
		return new ProcessDao(Tests.getDb()).insert(process);
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

	@Test
	public void test() {
		SystemCalculator calculator = new SystemCalculator(MatrixCache.createEager(Tests.getDb()),
				Tests.getDefaultSolver());
		CalculationSetup setup = new CalculationSetup(pSystem);
		setup.setAmount(1);
		ContributionResult cResult = calculator.calculateContributions(setup);
		DQResult result = DQResult.calculate(Tests.getDb(), cResult, pSystem.getId());
		Assert.assertArrayEquals(new int[] { 3, 4, 3, 3, 2 }, result.getFlowQuality(eFlow.getId()));
	}

	@After
	public void shutdown() {
		new ProductSystemDao(Tests.getDb()).delete(pSystem);
		new ProcessDao(Tests.getDb()).delete(process1);
		new ProcessDao(Tests.getDb()).delete(process2);
		new DQSystemDao(Tests.getDb()).delete(dqSystem);
		new FlowDao(Tests.getDb()).delete(pFlow1);
		new FlowDao(Tests.getDb()).delete(pFlow2);
		new FlowDao(Tests.getDb()).delete(eFlow);
		new FlowPropertyDao(Tests.getDb()).delete(property);
		new UnitGroupDao(Tests.getDb()).delete(unitGroup);
	}

}
