package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.openlca.core.database.BlockFetch;
import org.openlca.core.database.BlockFetch.QueryFunction;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.CostCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProcessCostEntry;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.results.SimpleCostResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostCalculator {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private CostMatrixSolver costSolver;
	private IDatabase database;

	public CostCalculator(IDatabase database) {
		costSolver = new CostMatrixSolver(database);
		this.database = database;
	}

	public SimpleCostResult calculate(ProductSystem system) {
		SimpleCostResult costResult = costSolver.calculate(system);
		applyFixCostsTo(costResult, system);
		return costResult;
	}

	private void applyFixCostsTo(SimpleCostResult costResult,
			ProductSystem system) {
		// Info: this index is also build earlier in the cost matrix builder
		List<Long> processIds = new ArrayList<>();
		Queue<Process> processes = new LinkedList<>();
		processes.add(system.getReferenceProcess());
		processIds.add(system.getReferenceProcess().getId());
		while (!processes.isEmpty()) {
			Process next = processes.poll();

			for (ProcessLink link : system.getIncomingLinks(next.getId())) {
				if (!processIds.contains(link.getProviderProcess().getRefId())) {
					processIds.add(link.getProviderProcess().getId());
					processes.add(link.getProviderProcess());
				}
			}
			for (ProcessLink link : system.getOutgoingLinks(next.getId())) {
				if (!processIds.contains(link.getRecipientProcess().getRefId())) {
					processIds.add(link.getRecipientProcess().getId());
					processes.add(link.getRecipientProcess());
				}
			}

		}

		List<ProcessCostEntry> costEntries = fetchFixCostEntries(processIds);
		if (costEntries == null || costEntries.isEmpty())
			return;

		Map<Long, Double> idToValue = new HashMap<>();
		Map<Long, CostCategory> idToCategory = new HashMap<>();
		for (ProcessCostEntry entry : costEntries) {
			CostCategory costCategory = entry.getCostCategory();

			// value map entry
			Double value = entry.getAmount();
			if (idToValue.containsKey(costCategory.getId()))
				value += idToValue.get(costCategory.getId());
			idToValue.put(costCategory.getId(), value);

			// category map entry
			if (!idToCategory.containsKey(costCategory.getId()))
				idToCategory.put(costCategory.getId(), costCategory);
		}

		CostCategory[] categories = new CostCategory[idToCategory.size()];
		double[] values = new double[idToCategory.size()];
		int index = 0;
		for (Long id : idToCategory.keySet()) {
			categories[index] = idToCategory.get(id);
			values[index] = idToValue.get(id);
			index++;
		}
		costResult.appendFixCosts(categories, values);
	}

	private List<ProcessCostEntry> fetchFixCostEntries(List<Long> productIds) {
		FixCostEntryQuery query = new FixCostEntryQuery();
		BlockFetch<ProcessCostEntry> fetch = new BlockFetch<>(query);
		return fetch.doFetch(productIds);
	}

	private class FixCostEntryQuery implements QueryFunction<ProcessCostEntry> {

		@Override
		public List<ProcessCostEntry> fetchChunk(List<Long> processIds) {
			try {
				String jpql = "select e from ProductCostEntry e where e.processId "
						+ "in :processIds AND e.costCategory.fix = true";
				return Query.on(database).getAll(ProcessCostEntry.class, jpql,
						Collections.singletonMap("processIds", processIds));
			} catch (Exception e) {
				log.error("Failed to fetch cost entries", e);
				return Collections.emptyList();
			}
		}

	}

}
