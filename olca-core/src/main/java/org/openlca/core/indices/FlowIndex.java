package org.openlca.core.indices;

import java.util.HashMap;
import java.util.List;

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

			}
		}
	}

}
