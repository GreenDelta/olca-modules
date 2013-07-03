package org.openlca.core.math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

/**
 * A matrix with the allocation factors for the technology and intervention
 * matrix of a product system. Internally we use a column-row-access for quick
 * testing whether there are allocation factors for a product (representing a
 * column in the matrices) available or not. The default factor is always 1.0.
 */
public class AllocationMatrix {

	private Map<Integer, Map<Integer, Double>> techColumns = new HashMap<>();
	private Map<Integer, Map<Integer, Double>> enviColumns = new HashMap<>();

	/** Create an allocation matrix with factors of the given method. */
	public static AllocationMatrix create(InventoryMatrix inventoryMatrix,
			ProductSystem system, AllocationMethod method, IDatabase database) {
		return new AllocationMatrix(inventoryMatrix, system, method, database);
	}

	/**
	 * Create an allocation matrix with factors for the default methods defined
	 * in the processes.
	 */
	public static AllocationMatrix create(InventoryMatrix inventoryMatrix,
			ProductSystem system, IDatabase database) {
		return new AllocationMatrix(inventoryMatrix, system, null, database);
	}

	private AllocationMatrix(InventoryMatrix inventoryMatrix,
			ProductSystem system, AllocationMethod method, IDatabase database) {
		AllocationSwitch aSwitch = new AllocationSwitch(method);
		for (Process process : system.getProcesses()) {
			List<Exchange> products = aSwitch.getTechOutputs(process);
			if (products.size() < 2)
				continue;
			if (match(process, method))
				fetchFromProcess(process, products, inventoryMatrix);
			else
				calculateFactors(process, aSwitch, inventoryMatrix);
		}
	}

	private boolean match(Process process, AllocationMethod method) {
		if (process.getAllocationMethod() == null)
			return false;
		if (process.getAllocationMethod() == method || method == null)
			return true;
		return false;
	}

	private void fetchFromProcess(Process process, List<Exchange> products,
			InventoryMatrix inventoryMatrix) {
		ProductIndex productIndex = inventoryMatrix.getProductIndex();
		FlowIndex flowIndex = inventoryMatrix.getFlowIndex();
		for (Exchange product : products) {
			int column = productIndex.getIndex(process, product);
			for (Exchange exchange : process.getExchanges()) {
				AllocationFactor f = exchange.getAllocationFactor(product
						.getRefId());
				if (f == null)
					continue;
				double factor = f.getValue();
				putFactor(productIndex, flowIndex, column, exchange, factor);
			}
		}
	}

	private void calculateFactors(Process process, AllocationSwitch aSwitch,
			InventoryMatrix inventoryMatrix) {
		ProductIndex productIndex = inventoryMatrix.getProductIndex();
		FlowIndex flowIndex = inventoryMatrix.getFlowIndex();
		Map<Exchange, Double> factors = aSwitch.getCommonFactors(process);
		for (Exchange product : factors.keySet()) {
			int column = productIndex.getIndex(process, product);
			double factor = factors.get(product);
			for (Exchange exchange : process.getExchanges())
				putFactor(productIndex, flowIndex, column, exchange, factor);
		}
	}

	private void putFactor(ProductIndex productIndex, FlowIndex flowIndex,
			int column, Exchange exchange, double factor) {
		if (productIndex.isLinkedInput(exchange)) {
			String key = productIndex.getLinkedOutputKey(exchange);
			int row = productIndex.getIndex(key);
			putFactor(row, column, factor, techColumns);
		} else if (flowIndex.contains(exchange.getFlow())) {
			int row = flowIndex.getIndex(exchange.getFlow());
			putFactor(row, column, factor, enviColumns);
		}
	}

	private void putFactor(int row, int col, double val,
			Map<Integer, Map<Integer, Double>> matrix) {
		if (row < 0 || col < 0)
			return;
		Map<Integer, Double> column = matrix.get(col);
		if (column == null) {
			column = new HashMap<>();
			matrix.put(col, column);
		}
		column.put(row, val);
	}

	/**
	 * Applies the allocation factors to the matrix cells of the inventory
	 * matrices.
	 */
	public void apply(InventoryMatrix matrix) {
		ProductIndex productIndex = matrix.getProductIndex();
		IMatrix techMatrix = matrix.getTechnologyMatrix();
		IMatrix enviMatrix = matrix.getInterventionMatrix();
		for (int col = 0; col < productIndex.size(); col++) {
			applyRows(techColumns.get(col), col, techMatrix);
			applyRows(enviColumns.get(col), col, enviMatrix);
		}
	}

	private void applyRows(Map<Integer, Double> rows, int col, IMatrix matrix) {
		if (rows == null || matrix == null)
			return;
		for (int row : rows.keySet()) {
			double factor = rows.get(row);
			if (factor != 1) {
				double val = matrix.getEntry(row, col);
				matrix.setEntry(row, col, factor * val);
			}
		}
	}

}
