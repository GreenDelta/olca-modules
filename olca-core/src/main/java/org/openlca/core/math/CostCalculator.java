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
import org.openlca.core.model.ProductCostEntry;
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
		List<String> processIds = new ArrayList<>();
		Queue<Process> processes = new LinkedList<>();
		processes.add(system.getReferenceProcess());
		processIds.add(system.getReferenceProcess().getRefId());
		while (!processes.isEmpty()) {
			Process next = processes.poll();

			for (ProcessLink link : system.getIncomingLinks(next.getRefId())) {
				if (!processIds.contains(link.getProviderProcess().getRefId())) {
					processIds.add(link.getProviderProcess().getRefId());
					processes.add(link.getProviderProcess());
				}
			}
			for (ProcessLink link : system.getOutgoingLinks(next.getRefId())) {
				if (!processIds.contains(link.getRecipientProcess().getRefId())) {
					processIds.add(link.getRecipientProcess().getRefId());
					processes.add(link.getRecipientProcess());
				}
			}

		}

		List<ProductCostEntry> costEntries = fetchFixCostEntries(processIds);
		if (costEntries == null || costEntries.isEmpty())
			return;

		Map<String, Double> idToValue = new HashMap<>();
		Map<String, CostCategory> idToCategory = new HashMap<>();
		for (ProductCostEntry entry : costEntries) {
			CostCategory costCategory = entry.getCostCategory();

			// value map entry
			Double value = entry.getAmount();
			if (idToValue.containsKey(costCategory.getRefId()))
				value += idToValue.get(costCategory.getRefId());
			idToValue.put(costCategory.getRefId(), value);

			// category map entry
			if (!idToCategory.containsKey(costCategory.getRefId()))
				idToCategory.put(costCategory.getRefId(), costCategory);
		}

		CostCategory[] categories = new CostCategory[idToCategory.size()];
		double[] values = new double[idToCategory.size()];
		int index = 0;
		for (String id : idToCategory.keySet()) {
			categories[index] = idToCategory.get(id);
			values[index] = idToValue.get(id);
			index++;
		}
		costResult.appendFixCosts(categories, values);
	}

	private List<ProductCostEntry> fetchFixCostEntries(List<String> productIds) {
		FixCostEntryQuery query = new FixCostEntryQuery();
		BlockFetch<ProductCostEntry> fetch = new BlockFetch<>(query);
		return fetch.doFetch(productIds);
	}

	private class FixCostEntryQuery implements QueryFunction<ProductCostEntry> {

		@Override
		public List<ProductCostEntry> fetchChunk(List<String> processIds) {
			try {
				String jpql = "select e from ProductCostEntry e where e.processId "
						+ "in :processIds AND e.costCategory.fix = true";
				return Query.on(database).getAll(ProductCostEntry.class, jpql,
						Collections.singletonMap("processIds", processIds));
			} catch (Exception e) {
				log.error("Failed to fetch cost entries", e);
				return Collections.emptyList();
			}
		}

	}

}
