package org.openlca.core.matrix;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a flow index from a product index and exchange table. All flows that
 * are not contained in the product index will be added to the flow index
 * (except if they are allocated co-products).
 */
class FlowIndexBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final MatrixCache cache;
	private final TechIndex productIndex;
	private final AllocationMethod allocationMethod;

	FlowIndexBuilder(MatrixCache cache, TechIndex productIndex,
			AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
		this.cache = cache;
		this.productIndex = productIndex;
	}

	FlowIndex build() {
		FlowIndex index = new FlowIndex();
		Map<Long, List<CalcExchange>> map = loadExchanges();
		for (Long processId : productIndex.getProcessIds()) {
			List<CalcExchange> exchanges = map.get(processId);
			for (CalcExchange e : exchanges) {
				if (index.contains(e.flowId))
					continue; // already indexed as flow
				LongPair productCandidate = new LongPair(e.processId, e.flowId);
				if (productIndex.contains(productCandidate))
					continue; // the exchange is an output product
				if (productIndex.isLinked(productCandidate))
					continue; // the exchange is a linked input
				if (e.input || e.flowType == FlowType.ELEMENTARY_FLOW)
					indexFlow(e, index);
				else if (allocationMethod == null
						|| allocationMethod == AllocationMethod.NONE)
					indexFlow(e, index); // non-allocated co-product -> handle
											// like elementary flow
			}
		}
		return index;
	}

	private Map<Long, List<CalcExchange>> loadExchanges() {
		try {
			Map<Long, List<CalcExchange>> map = cache.getExchangeCache()
					.getAll(productIndex.getProcessIds());
			return map;
		} catch (Exception e) {
			log.error("failed to load exchanges from cache", e);
			return Collections.emptyMap();
		}
	}

	private void indexFlow(CalcExchange e, FlowIndex index) {
		if (e.input)
			index.putInputFlow(e.flowId);
		else
			index.putOutputFlow(e.flowId);
	}

}
