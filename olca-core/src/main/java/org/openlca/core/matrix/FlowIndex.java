package org.openlca.core.matrix;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TLongByteHashMap;

/**
 * The row index $\mathit{Idx}_B$ of the intervention matrix $\mathbf{B}$. It
 * maps the (elementary) flows $\mathit{F}$ of the processes in the product
 * system to the $k$ rows of $\mathbf{B}$.
 *
 * $$\mathit{Idx}_B: \mathit{F} \mapsto [0 \dots k-1]$$
 */
public class FlowIndex extends DIndex<FlowDescriptor> {

	private TLongByteHashMap inputs = new TLongByteHashMap();

	// TODO: when we use blockm, we can build the flow index together
	// with the inventory matrices in a single table scan -> no matrix
	// cache is needed then anymore
	public static FlowIndex build(
			MatrixCache cache,
			TechIndex productIndex,
			AllocationMethod allocationMethod) {
		return new Builder(cache, productIndex, allocationMethod).build();
	}

	public int putInput(FlowDescriptor flow) {
		if (flow == null)
			return -1;
		inputs.put(flow.id, (byte) 1);
		return put(flow);
	}

	public int putOutput(FlowDescriptor flow) {
		if (flow == null)
			return -1;
		inputs.put(flow.id, (byte) 0);
		return put(flow);
	}

	public boolean isInput(FlowDescriptor flow) {
		if (flow == null)
			return false;
		return isInput(flow.id);
	}

	public boolean isInput(long flowId) {
		byte input = inputs.get(flowId);
		return input == 1;
	}

	/**
	 * Builds a flow index from a product index and exchange table. All flows
	 * that are not contained in the product index will be added to the flow
	 * index (except if they are allocated co-products).
	 */
	@Deprecated
	private static class Builder {

		private Logger log = LoggerFactory.getLogger(getClass());

		private final MatrixCache cache;
		private final TechIndex techIndex;
		private final AllocationMethod allocationMethod;
		private final FlowTable flows;

		Builder(MatrixCache cache,
				TechIndex techIndex,
				AllocationMethod allocationMethod) {
			this.allocationMethod = allocationMethod;
			this.cache = cache;
			this.techIndex = techIndex;
			flows = FlowTable.create(cache.getDatabase());
		}

		FlowIndex build() {
			FlowIndex index = new FlowIndex();
			Map<Long, List<CalcExchange>> map = loadExchanges();
			for (Long processId : techIndex.getProcessIds()) {
				List<CalcExchange> exchanges = map.get(processId);
				if (exchanges == null)
					continue;
				for (CalcExchange e : exchanges) {
					if (index.contains(e.flowId))
						continue; // already indexed as flow
					if (techIndex.contains(e.processId, e.flowId))
						continue; // the exchange is an output product
					if (techIndex
							.isLinked(LongPair.of(e.processId, e.exchangeId)))
						continue; // the exchange is a linked exchange
					if (e.flowType == FlowType.ELEMENTARY_FLOW)
						indexFlow(e, index);
					if (e.flowType == FlowType.PRODUCT_FLOW && e.isInput)
						indexFlow(e, index); // unlinked product inputs
					if (e.flowType == FlowType.WASTE_FLOW && !e.isInput)
						indexFlow(e, index); // unlinked waste outputs
					else if (allocationMethod == null
							|| allocationMethod == AllocationMethod.NONE)
						indexFlow(e, index); // non-allocated co-product or
												// waste
												// treatment -> handle
												// like elementary flow
				}
			}
			return index;
		}

		private Map<Long, List<CalcExchange>> loadExchanges() {
			try {
				// TODO: the cache loader throws an exception when we ask for
				// process IDs that do not exist; this we have to filter out
				// product system IDs here
				HashSet<Long> processIds = new HashSet<>();
				techIndex.each((i, p) -> {
					if (p.process != null
							&& p.process.type == ModelType.PROCESS) {
						processIds.add(p.process.id);
					}
				});
				Map<Long, List<CalcExchange>> map = cache.getExchangeCache()
						.getAll(processIds);
				return map;
			} catch (Exception e) {
				log.error("failed to load exchanges from cache", e);
				return Collections.emptyMap();
			}
		}

		private void indexFlow(CalcExchange e, FlowIndex index) {
			if (index.contains(e.flowId))
				return;
			if (e.isInput) {
				index.putInput(flows.get(e.flowId));
			} else {
				index.putOutput(flows.get(e.flowId));
			}
		}

	}
}
