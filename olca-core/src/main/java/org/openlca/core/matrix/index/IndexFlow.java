package org.openlca.core.matrix.index;

import java.util.Objects;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * Describes the mapping of flow information to a matrix index.
 */
public class IndexFlow {

	/**
	 * The flow that is mapped to the matrix index.
	 */
	public final FlowDescriptor flow;

	/**
	 * In case of a regionalized flow index flow-location pairs are mapped to matrix
	 * indices.
	 */
	public final LocationDescriptor location;

	/**
	 * Indicates whether the flow is an input flow or not.
	 */
	public final boolean isInput;

	IndexFlow(FlowDescriptor flow, LocationDescriptor location, boolean isInput) {
		this.flow = Objects.requireNonNull(flow);
		this.location = location;
		this.isInput = isInput;
	}

	public static IndexFlow inputOf(FlowDescriptor flow) {
		return new IndexFlow(flow, null, true);
	}

	public static IndexFlow inputOf(FlowDescriptor flow, LocationDescriptor loc) {
		return new IndexFlow(flow, loc, true);
	}

	public static IndexFlow outputOf(FlowDescriptor flow) {
		return new IndexFlow(flow, null, false);
	}

	public static IndexFlow outputOf(FlowDescriptor flow, LocationDescriptor loc) {
		return new IndexFlow(flow, loc, false);
	}

	long flowId() {
		return flow.id;
	}

	LongPair regionalizedId() {
		var locID = location == null ? 0L : location.id;
		return LongPair.of(flow.id, locID);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var other = (IndexFlow) o;
		return Objects.equals(flow, other.flow)
				&& Objects.equals(location, other.location);
	}

	@Override
	public int hashCode() {
		if (location == null)
			return (int) flow.id;
		return LongPair.hash(flow.id, location.id);
	}
}
