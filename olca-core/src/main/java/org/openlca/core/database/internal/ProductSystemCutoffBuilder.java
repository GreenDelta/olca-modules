package org.openlca.core.database.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IProductSystemBuilder;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of a product system builder with cut-off support. */
public class ProductSystemCutoffBuilder implements IProductSystemBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private double cutoff;
	private boolean preferSystemProcesses;

	private Map<String, Double> scalingFactors = new HashMap<>();

	private ProcessTypeTable processTypeTable;
	private ProductExchangeTable exchangeTable;
	private Set<ProductLink> newLinks = new HashSet<>();

	public ProductSystemCutoffBuilder(IDatabase database, double cutoff) {
		this.database = database;
		this.cutoff = cutoff;
		this.processTypeTable = new ProcessTypeTable(database);
		this.exchangeTable = new ProductExchangeTable(database);
	}

	public void setPreferSystemProcesses(boolean preferSystemProcesses) {
		this.preferSystemProcesses = preferSystemProcesses;
	}

	@Override
	public void autoComplete(ProductSystem system) {
		double sysAmount = system.getConvertedTargetAmount();
		double refAmount = system.getReferenceProcess()
				.getQuantitativeReference().getConvertedResult();
		autoComplete(system.getReferenceProcess(), sysAmount / refAmount);
		new ProductSystemTable(database).merge(system, newLinks);
	}

	@Override
	public void autoComplete(ProductSystem system, Process process) {
		autoComplete(process, 1);
		new ProductSystemTable(database).merge(system, newLinks);
	}

	private void autoComplete(Process process, double factor) {
		scalingFactors.put(process.getRefId(), factor);
		Set<String> handled = new HashSet<>();
		Queue<Collection<String>> nextLevels = new ArrayDeque<>();
		nextLevels.add(Arrays.asList(process.getRefId()));
		int n = 0;
		while (!nextLevels.isEmpty()) {
			Collection<String> processIds = nextLevels.poll();
			log.trace("load level {} with {} processes", n, processIds.size());
			exchangeTable.loadProductInputs(processIds);
			List<ProductExchange> allInputs = new ArrayList<>();
			for (String processId : processIds) {
				if (handled.contains(processId))
					continue;
				handled.add(processId);
				List<ProductExchange> inputs = getFilteredInputs(processId);
				allInputs.addAll(inputs);
			}
			Set<String> nextLevel = fetchNextLevel(allInputs);
			if (!nextLevel.isEmpty())
				nextLevels.add(nextLevel);
			n++;
		}
	}

	private List<ProductExchange> getFilteredInputs(String processId) {
		List<ProductExchange> inputs = exchangeTable
				.getProductInputs(processId);
		if (cutoff == 0 || inputs.isEmpty())
			return inputs;
		Double scalingFactor = scalingFactors.get(processId);
		if (scalingFactor == null)
			return inputs;
		double factor = scalingFactor;
		List<ProductExchange> filtered = new ArrayList<>();
		for (ProductExchange input : inputs) {
			double amount = input.getAmount();
			if (cutoff < Math.abs(factor * amount))
				filtered.add(input);
		}
		return filtered;
	}

	private Set<String> fetchNextLevel(List<ProductExchange> inputs) {
		loadTableData(inputs);
		Set<String> nextProcessIds = new HashSet<>();
		Set<ProductLink> nextLinks = fetchNextLinks(inputs);
		for (ProductLink nextLink : nextLinks) {
			ProductExchange input = nextLink.getInput();
			ProductExchange output = nextLink.getOutput();
			double preFactor = scalingFactors.get(input.getProcessId());
			double nextFactor = preFactor * nextLink.getScalingFactor();
			Double oldFactor = scalingFactors.get(output.getProcessId());
			if (oldFactor == null)
				scalingFactors.put(output.getProcessId(), nextFactor);
			else if (nextFactor > oldFactor) {
				// TODO: update
				scalingFactors.put(output.getProcessId(), nextFactor);
			}
			newLinks.add(nextLink);
			nextProcessIds.add(output.getProcessId());
		}
		return nextProcessIds;
	}

	private Set<ProductLink> fetchNextLinks(List<ProductExchange> inputs) {
		Set<ProductLink> nextLinks = new HashSet<>();
		for (ProductExchange input : inputs) {
			ProductExchange output = findBestOutput(input);
			if (output == null)
				continue;
			ProductLink nextLink = new ProductLink(input, output);
			nextLinks.add(nextLink);
		}
		return nextLinks;
	}

	private ProductExchange findBestOutput(ProductExchange input) {
		List<ProductExchange> outputs = exchangeTable.getOutputsWithFlow(input
				.getFlowId());
		ProductExchange candidate = null;
		for (ProductExchange output : outputs) {
			String defaultId = input.getDefaultProviderId();
			String providerId = output.getProcessId();
			if (defaultId != null && defaultId.equals(providerId))
				return output;
			if (betterOutputCandidate(output, candidate))
				candidate = output;
		}
		return candidate;
	}

	private boolean betterOutputCandidate(ProductExchange newCandidate,
			ProductExchange oldCandidate) {
		if (oldCandidate == null)
			return true;
		ProcessType preferredType = preferSystemProcesses ? ProcessType.LCI_Result
				: ProcessType.UnitProcess;
		ProcessType newType = processTypeTable.getType(newCandidate
				.getProcessId());
		ProcessType oldType = processTypeTable.getType(oldCandidate
				.getProcessId());
		if (oldType != preferredType && newType == preferredType)
			return true;
		if (oldType == preferredType && newType != preferredType)
			return false;
		return newCandidate.getAmount() > oldCandidate.getAmount();
	}

	private void loadTableData(List<ProductExchange> inputs) {
		Set<String> flowIds = new HashSet<>();
		for (ProductExchange input : inputs)
			flowIds.add(input.getFlowId());
		exchangeTable.loadOutputsWithFlows(flowIds);
		Set<String> newProcessIds = new HashSet<>();
		for (ProductExchange input : inputs) {
			List<ProductExchange> outputs = exchangeTable
					.getOutputsWithFlow(input.getFlowId());
			for (ProductExchange output : outputs)
				newProcessIds.add(output.getProcessId());
		}
		processTypeTable.load(newProcessIds);
	}

}
