package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ProcessLink;

class ProcessLinkIndex {

	private final TLongObjectHashMap<TLongObjectHashMap<TLongArrayList>> index;

	public ProcessLinkIndex() {
		index = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
	}

	public boolean contains(ProcessLink link) {
		return contains(link.getProviderId(), link.getRecipientId(),
				link.getFlowId());
	}

	public boolean contains(long provider, long recipient, long flow) {
		TLongObjectHashMap<TLongArrayList> recFlowMap = index.get(provider);
		if (recFlowMap == null)
			return false;
		TLongArrayList flowIds = recFlowMap.get(recipient);
		if (flowIds == null)
			return false;
		return flowIds.contains(flow);
	}

	public void put(ProcessLink link) {
		if (link == null)
			return;
		put(link.getProviderId(), link.getRecipientId(), link.getFlowId());
	}

	public void put(long provider, long recipient, long flow) {
		TLongObjectHashMap<TLongArrayList> recFlowMap = index.get(provider);
		if (recFlowMap == null) {
			recFlowMap = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR, -1);
			index.put(provider, recFlowMap);
		}
		TLongArrayList flowIds = recFlowMap.get(recipient);
		if (flowIds == null) {
			flowIds = new TLongArrayList();
			recFlowMap.put(recipient, flowIds);
		}
		flowIds.add(flow);
	}

	public List<ProcessLink> createLinks() {
		List<ProcessLink> links = new ArrayList<>();
		for (long provider : index.keys()) {
			TLongObjectHashMap<TLongArrayList> recFlowMap = index.get(provider);
			if (recFlowMap == null)
				continue;
			for (long recipient : recFlowMap.keys()) {
				TLongArrayList flowIds = recFlowMap.get(recipient);
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
