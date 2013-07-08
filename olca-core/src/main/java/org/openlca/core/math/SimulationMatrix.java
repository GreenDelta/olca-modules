package org.openlca.core.math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.UncertaintyDistributionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A matrix that decorates an inventory matrix with uncertainty information.
 */
class SimulationMatrix {

	private final int TECH_TYPE = 0;
	private final int ENVI_TYPE = 1;

	private Logger log = LoggerFactory.getLogger(getClass());
	private Map<Integer, Map<Integer, SimulationBucket>> techBuckets;
	private Map<Integer, Map<Integer, SimulationBucket>> enviBuckets;
	private InventoryMatrix inventoryMatrix;
	private FlowIndex flowIndex;
	private ProductIndex productIndex;

	private SimulationMatrix(InventoryMatrix inventoryMatrix) {
		techBuckets = new HashMap<>();
		enviBuckets = new HashMap<>();
		this.inventoryMatrix = inventoryMatrix;
		this.flowIndex = inventoryMatrix.getFlowIndex();
		this.productIndex = inventoryMatrix.getProductIndex();
	}

	public static SimulationMatrix create(ProductSystem system) {
		InventoryMatrixBuilder builder = new InventoryMatrixBuilder(system);
		InventoryMatrix inventoryMatrix = builder.build();
		SimulationMatrix matrix = new SimulationMatrix(inventoryMatrix);
		matrix.decorate(system);
		return matrix;
	}

	InventoryMatrix getInventoryMatrix() {
		return inventoryMatrix;
	}

	private void decorate(ProductSystem system) {
		log.trace("decorate inventory matrix with number generators");
		for (Process process : system.getProcesses()) {
			for (Exchange exchange : process.getExchanges()) {
				if (noDistribution(exchange))
					continue;
				SimulationBucket bucket = new SimulationBucket(exchange);
				setBucket(bucket, process, exchange);
			}
		}
	}

	private boolean noDistribution(Exchange exchange) {
		return exchange.getDistributionType() == null
				|| exchange.getDistributionType() == UncertaintyDistributionType.NONE;
	}

	private void setBucket(SimulationBucket bucket, Process process,
			Exchange exchange) {
		if (productIndex.contains(process, exchange)) {
			int idx = productIndex.getIndex(process, exchange);
			putBucket(idx, idx, bucket, TECH_TYPE);
		} else if (productIndex.isLinkedInput(exchange)) {
			putTechInputBuckets(bucket, process, exchange);
		} else if (flowIndex.contains(exchange.getFlow())) {
			putEnviBuckets(bucket, process, exchange);
		}
	}

	private void putTechInputBuckets(SimulationBucket bucket, Process process,
			Exchange input) {
		List<Long> processProducts = productIndex.getProducts(process);
		Long outputKey = productIndex.getLinkedOutputKey(input);
		int row = productIndex.getIndex(outputKey);
		for (Long productId : processProducts) {
			int col = productIndex.getIndex(productId);
			if (row < 0 || col < 0)
				continue;
			putBucket(row, col, bucket, TECH_TYPE);
		}
	}

	private void putEnviBuckets(SimulationBucket bucket, Process process,
			Exchange exchange) {
		int row = flowIndex.getIndex(exchange.getFlow());
		List<Long> processProducts = productIndex.getProducts(process);
		for (Long productId : processProducts) {
			int col = productIndex.getIndex(productId);
			if (row < 0 || col < 0)
				continue;
			putBucket(row, col, bucket, ENVI_TYPE);
		}
	}

	private void putBucket(int row, int col, SimulationBucket bucket, int type) {
		Map<Integer, Map<Integer, SimulationBucket>> matrix = type == TECH_TYPE ? techBuckets
				: enviBuckets;
		Map<Integer, SimulationBucket> rowEntries = matrix.get(row);
		if (rowEntries == null) {
			rowEntries = new HashMap<>();
			matrix.put(row, rowEntries);
		}
		rowEntries.put(col, bucket);
	}

	/**
	 * Returns the live matrix which is not intended to be written outside of
	 * this class.
	 */
	public InventoryMatrix nextRun() {
		log.trace("generate next matrix values");
		fillMatrix(inventoryMatrix.getTechnologyMatrix(), techBuckets);
		fillMatrix(inventoryMatrix.getInterventionMatrix(), enviBuckets);
		return inventoryMatrix;
	}

	private void fillMatrix(IMatrix matrix,
			Map<Integer, Map<Integer, SimulationBucket>> buckets) {
		for (int row : buckets.keySet()) {
			Map<Integer, SimulationBucket> rowEntries = buckets.get(row);
			if (rowEntries == null)
				continue;
			for (int col : rowEntries.keySet()) {
				SimulationBucket bucket = rowEntries.get(col);
				if (bucket == null)
					continue;
				double nextVal = bucket.nextValue();
				matrix.setEntry(row, col, nextVal);
			}
		}
	}

}
