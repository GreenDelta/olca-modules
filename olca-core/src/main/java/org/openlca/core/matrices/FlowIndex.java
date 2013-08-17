package org.openlca.core.matrices;

import gnu.trove.map.hash.TLongByteHashMap;

import java.util.List;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;

/**
 * A flow index represents the flows in the intervention matrix. Thus, this
 * index maps each flow to a row in the intervention matrix.
 */
public class FlowIndex {

	private LongIndex flowIndex = new LongIndex();
	private TLongByteHashMap inputMap = new TLongByteHashMap();

	FlowIndex(ProductIndex productIndex, ExchangeTable exchangeTable,
			AllocationMethod allocationMethod) {
		init(productIndex, exchangeTable, allocationMethod);
	}

	private void init(ProductIndex productIndex, ExchangeTable exchangeTable,
			AllocationMethod allocationMethod) {
		for (Long processId : exchangeTable.getProcessIds()) {
			List<CalcExchange> exchanges = exchangeTable
					.getExchanges(processId);
			for (CalcExchange e : exchanges) {
				if (flowIndex.contains(e.getFlowId()))
					continue; // already indexed as flow
				LongPair productCandidate = new LongPair(e.getProcessId(),
						e.getFlowId());
				if (productIndex.contains(productCandidate))
					continue; // the exchange is an output product
				if (productIndex.isLinkedInput(productCandidate))
					continue; // the exchange is a linked input
				if (e.isInput() || e.getFlowType() == FlowType.ELEMENTARY_FLOW)
					indexFlow(e);
				else if (allocationMethod == null
						|| allocationMethod == AllocationMethod.NONE)
					indexFlow(e); // non-allocated co-product -> handle like
									// elementary flow
			}
		}
	}

	private void indexFlow(CalcExchange e) {
		flowIndex.put(e.getFlowId());
		byte input = e.isInput() ? (byte) 1 : (byte) 0;
		inputMap.put(e.getFlowId(), input);
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
