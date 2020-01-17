package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongByteHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A flow index that supports regionalization.
 */
public final class RegFlowIndex {

	private final HashMap<LongPair, Integer> index = new HashMap<>();
	private final ArrayList<FlowDescriptor> flows = new ArrayList<>();

	/**
	 * Contains the location at index i. The value may be null if the
	 * flow at this location is not regionalized.
	 */
	private final ArrayList<LocationDescriptor> locations = new ArrayList<>();

	private final TLongByteHashMap input = new TLongByteHashMap(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			-1L,        // no entry key
			(byte) 0);  // no entry value

	TLongObjectHashMap<TIntArrayList> flowPos = new TLongObjectHashMap<>();

	public int size() {
		return index.size();
	}

	public boolean isEmpty() {
		return index.isEmpty();
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
	 * index. Note that the returned value is `null` when the flow at the
	 * index is not regionalized.
	 */
	public LocationDescriptor locationAt(int i) {
		if (i < 0 || i > locations.size())
			return null;
		return locations.get(i);
	}

	public int of(FlowDescriptor flow) {
		return of(flow, null);
	}

	/**
	 * Returns the matrix index of the given flow and location pair.
	 */
	public int of(FlowDescriptor flow, LocationDescriptor location) {
		if (flow == null)
			return -1;
		long locID = location == null ? 0L : location.id;
		LongPair p = LongPair.of(flow.id, locID);
		Integer i = index.get(p);
		return i == null ? -1 : i;
	}

	public int of(long flowID) {
		return of(flowID, 0L);
	}


	/**
	 * Returns the matrix index of the given flow and location pair.
	 */
	public int of(long flowID, long locationID) {
		LongPair p = LongPair.of(flowID, locationID);
		Integer i = index.get(p);
		return i == null ? -1 : i;
	}

	public boolean contains(FlowDescriptor flow) {
		return of(flow) >= 0;
	}

	/**
	 * Returns true when the given flow and location is contained in this index.
	 */
	public boolean contains(FlowDescriptor flow, LocationDescriptor location) {
		return of(flow, location) >= 0;
	}

	public boolean contains(long flowID) {
		return of(flowID) >= 0;
	}

	/**
	 * Returns true when the given flow and location is contained in this index.
	 */
	public boolean contains(long flowID, long locationID) {
		return of(flowID, locationID) >= 0;
	}

	public int putInput(FlowDescriptor flow) {
		return put(flow, null, true);
	}

	public int putInput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, true);
	}

	public int putOutput(FlowDescriptor flow) {
		return put(flow, null, false);
	}

	public int putOutput(FlowDescriptor flow, LocationDescriptor location) {
		return put(flow, location, false);
	}

	private int put(FlowDescriptor flow, LocationDescriptor location,
	                boolean isInput) {
		if (flow == null)
			return -1;
		long locID = location == null ? 0L : location.id;
		LongPair p = LongPair.of(flow.id, locID);
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
		TIntArrayList pos = flowPos.get(flow.id);
		if (pos == null) {
			// -1 is our `no-entry` value as only
			// values >= 0 are correct matrix
			// indices. 10 is the default
			// capacity
			pos = new TIntArrayList(
					Constants.DEFAULT_CAPACITY, -1);
			flowPos.put(flow.id, pos);
		}
		pos.add(idx);
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

	public void each(Consumer fn) {
		for (int i = 0; i < flows.size(); i++) {
			fn.accept(i, flows.get(i), locations.get(i));
		}
	}

	public Set<FlowDescriptor> getFlows() {
		return new HashSet<>(flows);
	}

	public Set<LocationDescriptor> getLocations() {
		return locations.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	/**
	 * A flow can occur multiple times in this index with different
	 * locations. This method returns all of these positions where
	 * the given flow occurs. Note that for performance reasons this
	 * method returns a live list which you should never modify
	 * outside of the flow index.
	 */
	public TIntList getPositions(FlowDescriptor flow) {
		if (flow == null)
			return new TIntArrayList(0, -1);
		TIntArrayList pos = flowPos.get(flow.id);
		return pos != null ? pos : new TIntArrayList(0, -1);
	}

	@FunctionalInterface
	public interface Consumer {
		void accept(int index, FlowDescriptor flow,
		            LocationDescriptor location);
	}

}
