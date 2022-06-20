package org.openlca.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;

public class TestSystem {

	private final List<Process> processes = new ArrayList<>();
	private final Map<Long, Process> providers = new HashMap<>();
	private final ProductSystem system;

	private TestSystem(Process refProcess) {
		system = new ProductSystem();
		system.refId = UUID.randomUUID().toString();
		system.name = refProcess.name;
		system.referenceProcess = refProcess;
		Exchange qRef = refProcess.quantitativeReference;
		system.referenceExchange = qRef;
		system.targetAmount = qRef.amount;
		system.targetFlowPropertyFactor = qRef.flowPropertyFactor;
		system.targetUnit = qRef.unit;
		index(refProcess);
	}

	public static TestSystem of(Process refProcess) {
		return new TestSystem(refProcess);
	}

	private void index(Process process) {
		system.processes.add(process.id);
		processes.add(process);
		for (Exchange e : process.exchanges) {
			if (!isProvider(e))
				continue;
			long flowId = e.flow.id;
			providers.putIfAbsent(flowId, process);
		}
	}

	public TestSystem link(Process process) {
		if (processes.contains(process))
			return this;
		index(process);
		for (Process p : processes) {
			for (Exchange e : p.exchanges) {
				if (isProvider(e))
					continue;
				if (e.flow == null
						|| e.flow.flowType == FlowType.ELEMENTARY_FLOW)
					continue;
				long flowId = e.flow.id;
				Process provider = providers.get(flowId);
				if (provider == null)
					continue;
				ProcessLink link = new ProcessLink();
				link.providerId = provider.id;
				link.flowId = flowId;
				link.processId = p.id;
				link.exchangeId = e.id;
				if (!system.processLinks.contains(link)) {
					system.processLinks.add(link);
				}
			}
		}
		return this;
	}

	private static boolean isProvider(Exchange e) {
		if (e == null || e.flow == null)
			return false;
		FlowType type = e.flow.flowType;
		if (type == FlowType.PRODUCT_FLOW && !e.isInput)
			return true;
		return type == FlowType.WASTE_FLOW && e.isInput;
	}

	public ProductSystem get() {
		ProductSystemDao dao = new ProductSystemDao(Tests.getDb());
		return dao.insert(system);
	}

	public static FullResult calculate(ProductSystem system) {
		var setup = CalculationSetup.fullAnalysis(system)
			.withCosts(true);
		return calculate(setup);
	}

	public static FullResult calculate(CalculationSetup setup) {
		var calc = new SystemCalculator(Tests.getDb());
		return calc.calculateFull(setup);
	}

	public static ContributionResult contributions(ProductSystem system) {
		var setup = CalculationSetup.contributions(system).withCosts(true);
		var calc = new SystemCalculator(Tests.getDb());
		return calc.calculateContributions(setup);
	}

}
