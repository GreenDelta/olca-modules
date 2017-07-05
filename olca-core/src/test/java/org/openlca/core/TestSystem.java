package org.openlca.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.FullResultProvider;

public class TestSystem {

	private List<Process> processes = new ArrayList<>();
	private Map<Long, Process> providers = new HashMap<>();

	private ProductSystem system;

	private TestSystem(Process refProcess) {
		system = new ProductSystem();
		system.setRefId(UUID.randomUUID().toString());
		system.setName(refProcess.getName());
		system.setReferenceProcess(refProcess);
		Exchange qRef = refProcess.getQuantitativeReference();
		system.setReferenceExchange(qRef);
		system.setTargetAmount(qRef.amount);
		system.setTargetFlowPropertyFactor(qRef.flowPropertyFactor);
		system.setTargetUnit(qRef.unit);
		index(refProcess);
	}

	public static TestSystem of(Process refProcess) {
		return new TestSystem(refProcess);
	}

	private void index(Process process) {
		system.getProcesses().add(process.getId());
		processes.add(process);
		for (Exchange e : process.getExchanges()) {
			if (!isProvider(e))
				continue;
			long flowId = e.flow.getId();
			if (providers.get(flowId) == null) {
				providers.put(flowId, process);
			}
		}
	}

	public TestSystem link(Process process) {
		if (processes.contains(process))
			return this;
		index(process);
		for (Process p : processes) {
			for (Exchange e : p.getExchanges()) {
				if (isProvider(e))
					continue;
				if (e.flow == null || e.flow.getFlowType() == FlowType.ELEMENTARY_FLOW)
					continue;
				long flowId = e.flow.getId();
				Process provider = providers.get(flowId);
				if (provider == null)
					continue;
				ProcessLink link = new ProcessLink();
				link.providerId = provider.getId();
				link.flowId = flowId;
				link.processId = p.getId();
				link.exchangeId = e.getId();
				if (!system.getProcessLinks().contains(link)) {
					system.getProcessLinks().add(link);
				}
			}
		}
		return this;
	}

	private static boolean isProvider(Exchange e) {
		if (e == null || e.flow == null)
			return false;
		FlowType type = e.flow.getFlowType();
		if (type == FlowType.PRODUCT_FLOW && !e.isInput)
			return true;
		if (type == FlowType.WASTE_FLOW && e.isInput)
			return true;
		return false;
	}

	public ProductSystem get() {
		ProductSystemDao dao = new ProductSystemDao(Tests.getDb());
		return dao.insert(system);
	}

	public static FullResultProvider calculate(ProductSystem system) {
		CalculationSetup setup = new CalculationSetup(system);
		setup.withCosts = true;
		return calculate(setup);
	}

	public static FullResultProvider calculate(CalculationSetup setup) {
		SystemCalculator calc = new SystemCalculator(
				MatrixCache.createEager(Tests.getDb()),
				Tests.getDefaultSolver());
		FullResult fr = calc.calculateFull(setup);
		return new FullResultProvider(fr, EntityCache.create(Tests.getDb()));
	}

	public static ContributionResultProvider<ContributionResult> contributions(
			ProductSystem system) {
		CalculationSetup setup = new CalculationSetup(system);
		setup.withCosts = true;
		SystemCalculator calc = new SystemCalculator(
				MatrixCache.createEager(Tests.getDb()),
				Tests.getDefaultSolver());
		ContributionResult cr = calc.calculateContributions(setup);
		return new ContributionResultProvider<>(cr, EntityCache.create(Tests.getDb()));
	}

}
