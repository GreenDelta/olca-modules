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
	private final TechIndex techIndex;
	private final AllocationMethod allocationMethod;

	FlowIndexBuilder(MatrixCache cache, TechIndex techIndex,
			AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
		this.cache = cache;
		this.techIndex = techIndex;
	}

	FlowIndex build() {
		FlowIndex index = new FlowIndex();
		Map<Long, List<CalcExchange>> map = loadExchanges();
		for (Long processId : techIndex.getProcessIds()) {
			List<CalcExchange> exchanges = map.get(processId);
			for (CalcExchange e : exchanges) {
				if (index.contains(e.flowId))
					continue; // already indexed as flow
				if (techIndex.contains(LongPair.of(e.processId, e.flowId)))
					continue; // the exchange is an output product
				if (techIndex.isLinked(LongPair.of(e.processId, e.exchangeId)))
					continue; // the exchange is a linked exchange
				if (e.flowType == FlowType.ELEMENTARY_FLOW)
					indexFlow(e, index);
				if (e.flowType == FlowType.PRODUCT_FLOW && e.isInput)
					indexFlow(e, index); // unlinked product inputs
				if (e.flowType == FlowType.WASTE_FLOW && !e.isInput)
					indexFlow(e, index); // unlinked waste outputs
				else if (allocationMethod == null
						|| allocationMethod == AllocationMethod.NONE)
					indexFlow(e, index); // non-allocated co-product or waste
											// treatment -> handle
											// like elementary flow
			}
		}
		return index;
	}

	private Map<Long, List<CalcExchange>> loadExchanges() {
		try {
			Map<Long, List<CalcExchange>> map = cache.getExchangeCache()
					.getAll(techIndex.getProcessIds());
			return map;
		} catch (Exception e) {
			log.error("failed to load exchanges from cache", e);
			return Collections.emptyMap();
		}
	}

	private void indexFlow(CalcExchange e, FlowIndex index) {
		if (e.isInput)
			index.putInputFlow(e.flowId);
		else
			index.putOutputFlow(e.flowId);
	}

}
