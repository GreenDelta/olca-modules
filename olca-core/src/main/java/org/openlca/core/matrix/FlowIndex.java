package org.openlca.core.matrix;

import gnu.trove.map.hash.TLongByteHashMap;

import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;

/**
 * A flow index represents the flows in the intervention matrix. Thus, this
 * index maps each flow to a row in the intervention matrix.
 */
public class FlowIndex {

	private LongIndex flowIndex = new LongIndex();
	private TLongByteHashMap inputMap = new TLongByteHashMap();

	public static FlowIndex build(MatrixCache cache, TechIndex productIndex,
			AllocationMethod allocationMethod) {
		return new FlowIndexBuilder(cache, productIndex, allocationMethod)
				.build();
	}

	public void putInputFlow(long flowId) {
		flowIndex.put(flowId);
		inputMap.put(flowId, (byte) 1);
	}

	public void putOutputFlow(long flowId) {
		flowIndex.put(flowId);
		inputMap.put(flowId, (byte) 0);
	}

	public int getIndex(long flowId) {
		return flowIndex.getIndex(flowId);
	}

	public long getFlowAt(int idx) {
		return flowIndex.getKeyAt(idx);
	}

	public boolean contains(long flowId) {
		return flowIndex.contains(flowId);
	}

	public boolean isInput(long flowId) {
		byte input = inputMap.get(flowId);
		return input == 1;
	}

	public boolean isEmpty() {
		return flowIndex.isEmpty();
	}

	public long[] getFlowIds() {
		return flowIndex.getKeys();
	}

	public int size() {
		return flowIndex.size();
	}

}
