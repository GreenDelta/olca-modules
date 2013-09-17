package org.openlca.core.matrices;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.math.IMatrix;
import org.openlca.core.model.UncertaintyType;
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

	public SimulationMatrix(InventoryMatrix inventoryMatrix,
			ExchangeTable exchangeTable) {
		techBuckets = new HashMap<>();
		enviBuckets = new HashMap<>();
		this.inventoryMatrix = inventoryMatrix;
		this.flowIndex = inventoryMatrix.getFlowIndex();
		this.productIndex = inventoryMatrix.getProductIndex();
		decorate(exchangeTable);
	}

	InventoryMatrix getInventoryMatrix() {
		return inventoryMatrix;
	}

	private void decorate(ExchangeTable exchangeTable) {
		log.trace("decorate inventory matrix with number generators");
		for (int i = 0; i < productIndex.size(); i++) {
			LongPair processProduct = productIndex.getProductAt(i);
			for (CalcExchange exchange : exchangeTable.getVector(processProduct
					.getFirst())) {
				if (noDistribution(exchange))
					continue;
				SimulationBucket bucket = new SimulationBucket(exchange);
				setBucket(bucket, processProduct, exchange);
			}
		}
	}

	private boolean noDistribution(CalcExchange exchange) {
		return exchange.getUncertaintyType() == null
				|| exchange.getUncertaintyType() == UncertaintyType.NONE;
	}

	private void setBucket(SimulationBucket bucket, LongPair processProduct,
			CalcExchange exchange) {
		LongPair exchangeDef = new LongPair(exchange.getProcessId(),
				exchange.getFlowId());
		if (!exchange.isInput() && processProduct.equals(exchangeDef)) {
			int idx = productIndex.getIndex(processProduct);
			putBucket(idx, idx, bucket, TECH_TYPE);
		} else if (productIndex.isLinkedInput(exchangeDef)) {
			putTechInputBucket(bucket, processProduct, exchange);
		} else if (flowIndex.contains(exchange.getFlowId())) {
			putEnviBuckets(bucket, processProduct, exchange);
		}
	}

	private void putTechInputBucket(SimulationBucket bucket,
			LongPair processProduct, CalcExchange input) {
		LongPair inputProduct = new LongPair(input.getProcessId(),
				input.getFlowId());
		LongPair provider = productIndex.getLinkedOutput(inputProduct);
		int row = productIndex.getIndex(provider);
		int col = productIndex.getIndex(processProduct);
		if (row < 0 || col < 0)
			return;
		putBucket(row, col, bucket, TECH_TYPE);
	}

	private void putEnviBuckets(SimulationBucket bucket,
			LongPair processProduct, CalcExchange exchange) {
		int row = flowIndex.getIndex(exchange.getFlowId());
		int col = productIndex.getIndex(processProduct);
		if (row < 0 || col < 0)
			return;
		putBucket(row, col, bucket, ENVI_TYPE);
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
