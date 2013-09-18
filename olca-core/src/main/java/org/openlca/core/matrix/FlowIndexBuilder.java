package org.openlca.core.matrix;

import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.cache.ExchangeCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;

/**
 * Builds a flow index from a product index and exchange table. All flows that
 * are not contained in the product index will be added to the flow index
 * (except if they are allocated co-products).
 */
public class FlowIndexBuilder {

	private AllocationMethod allocationMethod;

	public FlowIndexBuilder(AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
	}

	public FlowIndex build(ProductIndex productIndex,
			ExchangeCache exchangeTable) {
		FlowIndex index = new FlowIndex();
		Map<Long, List<CalcExchange>> map = exchangeTable.getAll(productIndex
				.getProcessIds());
		for (Long processId : productIndex.getProcessIds()) {
			List<CalcExchange> exchanges = map.get(processId);
			for (CalcExchange e : exchanges) {
				if (index.contains(e.getFlowId()))
					continue; // already indexed as flow
				LongPair productCandidate = new LongPair(e.getProcessId(),
						e.getFlowId());
				if (productIndex.contains(productCandidate))
					continue; // the exchange is an output product
				if (productIndex.isLinkedInput(productCandidate))
					continue; // the exchange is a linked input
				if (e.isInput() || e.getFlowType() == FlowType.ELEMENTARY_FLOW)
					indexFlow(e, index);
				else if (allocationMethod == null
						|| allocationMethod == AllocationMethod.NONE)
					indexFlow(e, index); // non-allocated co-product -> handle
											// like elementary flow
			}
		}
		return index;
	}

	private void indexFlow(CalcExchange e, FlowIndex index) {
		if (e.isInput())
			index.putInputFlow(e.getFlowId());
		else
			index.putOutputFlow(e.getFlowId());
	}

}
