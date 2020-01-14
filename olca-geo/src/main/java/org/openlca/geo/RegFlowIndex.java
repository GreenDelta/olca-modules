package org.openlca.geo;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongByteHashMap;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A flow index that supports regionalization.
 */
public final class RegFlowIndex {

	private final HashMap<LongPair, Integer> index = new HashMap<>();
	private final ArrayList<FlowDescriptor> flows = new ArrayList<>();
	private final ArrayList<LocationDescriptor> locations = new ArrayList<>();
	private final TLongByteHashMap input = new TLongByteHashMap(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			-1L,        // no entry key
			(byte) 0);  // no entry value

	public int size() {
		return index.size();
	}

	/**
	 * Returns the flow at the given matrix index.
	 */
	public FlowDescriptor flowAt(int i) {
		if (i < 0 || i >= flows.size())
			return null;
		return flows.get(i);
	}

	/**
	 * Only for regionalized models: returns the location of the given matrix
	 * index.
	 */
	public LocationDescriptor locationAt(int i) {
		if (i < 0 || i > locations.size())
			return null;
		return locations.get(i);
	}

	/**
	 * Returns the matrix index of the given flow and location pair.
	 */
	public int of(FlowDescriptor flow, LocationDescriptor location) {
		if (flow == null || location == null)
			return -1;
		LongPair p = LongPair.of(flow.id, location.id);
		Integer i = index.get(p);
		return i == null ? -1 : i;
	}

	/**
	 * Returns the matrix index of the given flow and location pair.
	 */
	public int of(long flowID, long locationID) {
		LongPair p = LongPair.of(flowID, locationID);
		Integer i = index.get(p);
		return i == null ? -1 : i;
	}

	/**
	 * Returns true when the given flow and location is contained in this index.
	 */
	public boolean contains(FlowDescriptor flow, LocationDescriptor location) {
		return of(flow, location) >= 0;
	}

	/**
	 * Returns true when the given flow and location is contained in this index.
	 */
	public boolean contains(long flowID, long locationID) {
		return of(flowID, locationID) >= 0;
	}

	public int putInput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, true);
	}

	public int putOutput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, false);
	}

	private int put(FlowDescriptor flow, LocationDescriptor location,
	                boolean isInput) {
		if (flow == null || location == null)
			return -1;
		LongPair p = LongPair.of(flow.id, location.id);
		Integer i = index.get(p);
		if (i != null)
			return i;
		int idx = index.size();
		index.put(p, idx);
		flows.add(flow);
		locations.add(location);
		if (isInput) {
			input.put(flow.id, (byte) 1);
		}
		return idx;
	}

	public boolean isInput(FlowDescriptor flow) {
		if (flow == null)
			return false;
		return input.get(flow.id) == (byte) 1;
	}

	public boolean isInput(long flowId) {
		return input.get(flowId) == (byte) 1;
	}
}
