package org.openlca.core.matrix;

import gnu.trove.map.hash.TLongByteHashMap;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * A flow index that supports regionalization.
 */
public class RegionalizedFlowIndex {

	/**
	 * A map flowID -> boolean that stores whether a flow is an input flow or
	 * not.
	 */
	private TLongByteHashMap input = new TLongByteHashMap();

	/**
	 * Returns the flow at the given matrix index.
	 */
	public FlowDescriptor flowAt(int i) {
		// TODO: not yet implemented
		return null;
	}

	/**
	 * Only for regionalized models: returns the location of the given matrix
	 * index.
	 */
	public LocationDescriptor locationAt(int i) {
		// TODO: not yet implemented
		return null;
	}

	/**
	 * Returns the matrix index of the given flow and location pair.
	 */
	public int of(FlowDescriptor flow, LocationDescriptor location) {
		// TODO: not yet implemented
		return -1;
	}

	/**
	 * Returns the matrix index of the given flow and location pair.
	 */
	public int of(long flowID, long locationID) {
		// TODO: not yet implemented
		return -1;
	}

	/**
	 * Returns true when the given flow and location is contained in this index.
	 */
	public boolean contains(FlowDescriptor flow, LocationDescriptor location) {
		// TODO: not yet implemented
		return false;
	}

	/**
	 * Returns true when the given flow and location is contained in this index.
	 */
	public boolean contains(long flowID, long locationID) {
		// TODO: not yet implemented
		return false;
	}

	public int putInput(FlowDescriptor flow, LocationDescriptor location) {
		// TODO: not yet implemented
		return -1;
	}

	public int putOutput(FlowDescriptor flow, LocationDescriptor location) {
		// TODO: not yet implemented
		return -1;
	}

	public boolean isInput(FlowDescriptor flow) {
		if (flow == null)
			return false;
		return isInput(flow.id);
	}

	public boolean isInput(long flowId) {
		// TODO: not yet implemented
		return false;
	}
}
