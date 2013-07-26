package org.openlca.core.indices;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.model.FlowType;

/**
 * A flow index represents the flows in the intervention matrix. Thus, this
 * index maps each flow to a row in the intervention matrix.
 */
public class FlowIndex {

	private LongIndex flowIndex = new LongIndex();
	private HashMap<Long, Boolean> inputMap = new HashMap<>();

	public FlowIndex(ProductIndex productIndex, ExchangeTable exchangeTable) {
		init(productIndex, exchangeTable);
	}

	private void init(ProductIndex productIndex, ExchangeTable exchangeTable) {
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
				// TODO: co-products without allocation
			}
		}
	}

	private void indexFlow(CalcExchange e) {
		flowIndex.put(e.getFlowId());
		inputMap.put(e.getFlowId(), e.isInput());
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
		Boolean input = inputMap.get(flowId);
		if (input == null)
			return false;
		else
			return input;
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
