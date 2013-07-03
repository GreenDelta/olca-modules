package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.BlockFetch;
import org.openlca.core.database.BlockFetch.QueryFunction;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CostCategory;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductCostEntry;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostMatrixBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProductSystem productSystem;
	private ProductIndex productIndex;
	private Index<CostCategory> costCategoryIndex;
	private Map<String, List<ProductCostEntry>> productCostMap;
	private IDatabase database;
	private IMatrix technologyMatrix;
	private IMatrix costMatrix;

	public CostMatrixBuilder(ProductSystem system, IDatabase database) {
		this.productSystem = system;
		this.database = database;
	}

	public CostMatrix build() {
		productIndex = new ProductIndex(productSystem);
		List<String> productIds = productIndex.getProductIds();
		List<ProductCostEntry> costEntries = fetchVariableCostEntries(productIds);
		if (costEntries == null || costEntries.isEmpty())
			return CostMatrix.empty();
		createCostIndex(costEntries);
		int prods = productIndex.size();
		int cats = costCategoryIndex.size();
		this.technologyMatrix = MatrixFactory.create(prods, prods);
		this.costMatrix = MatrixFactory.create(cats, prods);
		fillMatrices();
		return makeMatrix();
	}

	private CostMatrix makeMatrix() {
		CostMatrix matrix = new CostMatrix();
		matrix.setCostCategoryIndex(costCategoryIndex);
		matrix.setCostMatrix(costMatrix);
		matrix.setProductIndex(productIndex);
		matrix.setTechnologyMatrix(technologyMatrix);
		return matrix;
	}

	private void createCostIndex(List<ProductCostEntry> costEntries) {
		costCategoryIndex = new Index<>(CostCategory.class);
		productCostMap = new HashMap<>();
		for (ProductCostEntry entry : costEntries) {
			CostCategory cc = entry.getCostCategory();
			String productId = entry.getExchangeId();
			if (cc == null || productId == null)
				continue;
			costCategoryIndex.put(cc);
			List<ProductCostEntry> productEntries = productCostMap
					.get(productId);
			if (productEntries == null) {
				productEntries = new ArrayList<>();
				productCostMap.put(productId, productEntries);
			}
			productEntries.add(entry);
		}
	}

	private List<ProductCostEntry> fetchVariableCostEntries(List<String> productIds) {
		VariableCostEntryQuery query = new VariableCostEntryQuery();
		BlockFetch<ProductCostEntry> fetch = new BlockFetch<>(query);
		return fetch.doFetch(productIds);
	}

	private void fillMatrices() {
		for (Process process : productSystem.getProcesses()) {
			for (Exchange exchange : process.getExchanges()) {
				if (productIndex.contains(process, exchange)) {
					makeTechnologyMatrixEntry(process, exchange);
					makeCostEntries(exchange);
				} else if (productIndex.isLinkedInput(exchange)) {
					makeTechnologyMatrixEntries(process, exchange);
				}
			}
		}
	}

	private void makeCostEntries(Exchange exchange) {
		List<ProductCostEntry> entries = productCostMap.get(exchange.getRefId());
		if (entries == null || entries.isEmpty())
			return;
		for (ProductCostEntry entry : entries) {
			int row = costCategoryIndex.getIndex(entry.getCostCategory());
			int col = productIndex.getIndex(exchange.getRefId());
			if (row >= 0 && col >= 0)
				costMatrix.setEntry(row, col, entry.getAmount());
		}
	}

	// TODO: same code as in intervention matrix builder

	private void makeTechnologyMatrixEntries(Process process, Exchange input) {
		List<String> processProducts = productIndex.getProducts(process);
		String outputKey = productIndex.getLinkedOutputKey(input);
		int row = productIndex.getIndex(outputKey);
		for (String productId : processProducts) {
			int col = productIndex.getIndex(productId);
			double oldValue = technologyMatrix.getEntry(row, col);
			double newValue = -1
					* getResultingAmount(input, productId,
							process.getAllocationMethod());
			if (input.isAvoidedProduct()) {
				newValue *= -1;
			}
			technologyMatrix.setEntry(row, col, oldValue + newValue);
		}
	}

	private double getResultingAmount(Exchange exchange, String productId,
			AllocationMethod allocationMethod) {
		double result = exchange.getConvertedResult();
		AllocationFactor allocationFactor = exchange
				.getAllocationFactor(productId);
		if (allocationMethod != null && allocationFactor != null
				&& allocationMethod != AllocationMethod.None) {
			result *= allocationFactor.getValue();
		}
		return result;
	}

	private void makeTechnologyMatrixEntry(Process process, Exchange output) {
		Integer idx = productIndex.getIndex(process, output);
		double oldValue = technologyMatrix.getEntry(idx, idx);
		double newValue = oldValue + output.getConvertedResult();
		technologyMatrix.setEntry(idx, idx, newValue);
	}

	private class VariableCostEntryQuery implements QueryFunction<ProductCostEntry> {

		@Override
		public List<ProductCostEntry> fetchChunk(List<String> productIds) {
			try {
				String jpql = "select e from ProductCostEntry e where e.exchangeId "
						+ "in :productIds AND e.costCategory.fix = false";
				return Query.on(database).getAll(ProductCostEntry.class, jpql,
						Collections.singletonMap("productIds", productIds));
			} catch (Exception e) {
				log.error("Failed to fetch cost entries", e);
				return Collections.emptyList();
			}
		}

	}

}
