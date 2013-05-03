package org.openlca.core.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.model.Flow;

/**
 * Maps a number of flows n to an index 0 <= i < n. For flows that are not
 * contained in the index i is -1. Additionally, a boolean flag for each flow
 * can be stored in this index to mark a flow as input (true) or output flow
 * (false, default).
 */
public class FlowIndex {

	private HashMap<String, Integer> map = new HashMap<>();
	private HashMap<String, Boolean> inputMap = new HashMap<>();
	private List<Flow> flows = new ArrayList<>();

	public void put(Flow flow) {
		if (contains(flow))
			return;
		int idx = map.size();
		map.put(flow.getId(), idx);
		flows.add(flow);
	}

	public int getIndex(Flow flow) {
		Integer idx = map.get(flow.getId());
		if (idx == null)
			return -1;
		return idx;
	}

	public Flow getFlowAt(int index) {
		return flows.get(index);
	}

	public int size() {
		return map.size();
	}

	public boolean contains(Flow flow) {
		return map.containsKey(flow.getId());
	}

	public void setInput(Flow flow, boolean b) {
		inputMap.put(flow.getId(), b);
	}

	public boolean isInput(Flow flow) {
		Boolean b = inputMap.get(flow.getId());
		return b != null && b.booleanValue();
	}

	public Flow[] getFlows() {
		return flows.toArray(new Flow[flows.size()]);
	}

	public boolean isEmpty() {
		return size() == 0;
	}

}
