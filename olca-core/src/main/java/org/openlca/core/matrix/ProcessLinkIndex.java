package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ProcessLink;

/**
 * An index of process links that is used for creation of product systems.
 */
class ProcessLinkIndex {

	/** Maps provider-process-id -> recipient-process-id -> product-flow-id. */
	private final TLongObjectHashMap<TLongObjectHashMap<TLongHashSet>> index;

	public ProcessLinkIndex() {
		index = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
	}

	/**
	 * Returns true if the link with the given provider, recipient, and flow is
	 * contained in this index.
	 */
	public boolean contains(ProcessLink link) {
		return contains(link.getProviderId(), link.getRecipientId(),
				link.getFlowId());
	}

	/**
	 * Returns true if the link with the given provider, recipient, and flow is
	 * contained in this index.
	 */
	public boolean contains(long provider, long recipient, long flow) {
		TLongObjectHashMap<TLongHashSet> recFlowMap = index.get(provider);
		if (recFlowMap == null)
			return false;
		TLongHashSet flowIds = recFlowMap.get(recipient);
		if (flowIds == null)
			return false;
		return flowIds.contains(flow);
	}

	/**
	 * Adds the given link to this index. Multiple inserts of the same link
	 * result in a single entry.
	 */
	public void put(ProcessLink link) {
		if (link == null)
			return;
		put(link.getProviderId(), link.getRecipientId(), link.getFlowId());
	}

	/**
	 * Adds a new link with the given provider, recipient, and flow to this
	 * index. Multiple inserts of the same triple result in a single entry.
	 */
	public void put(long provider, long recipient, long flow) {
		TLongObjectHashMap<TLongHashSet> recFlowMap = index.get(provider);
		if (recFlowMap == null) {
			recFlowMap = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR, -1);
			index.put(provider, recFlowMap);
		}
		TLongHashSet flowIds = recFlowMap.get(recipient);
		if (flowIds == null) {
			flowIds = new TLongHashSet();
			recFlowMap.put(recipient, flowIds);
		}
		flowIds.add(flow);
	}

	/**
	 * Creates a new list of process links from this index.
	 */
	public List<ProcessLink> createLinks() {
		List<ProcessLink> links = new ArrayList<>();
		for (long provider : index.keys()) {
			TLongObjectHashMap<TLongHashSet> recFlowMap = index.get(provider);
			if (recFlowMap == null)
				continue;
			for (long recipient : recFlowMap.keys()) {
				TLongHashSet flowIds = recFlowMap.get(recipient);
				if (flowIds == null)
					continue;
				for (long flow : flowIds.toArray()) {
					ProcessLink link = new ProcessLink();
					link.setFlowId(flow);
					link.setProviderId(provider);
					link.setRecipientId(recipient);
					links.add(link);
				}
			}
		}
		return links;
	}

}
