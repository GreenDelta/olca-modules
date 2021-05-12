package org.openlca.core.matrix.index;

import java.util.Objects;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * Describes the mapping of flow information to a matrix index.
 */
public class IndexFlow {

	private final FlowDescriptor flow;
	private final LocationDescriptor location;
	private final boolean isInput;

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

	/**
	 * Returns the flow descriptor which is never {@code null}.
	 */
	public FlowDescriptor flow() {
		return flow;
	}

	/**
	 * Returns the location descriptor. This returns {@code null} fo
	 * non-regionalized indices.
	 */
	public LocationDescriptor location() {
		return location;
	}

	/**
	 * Returns {@code true} when this flow is an input flow, otherwise
	 * {@code false}.
	 */
	public boolean isInput() {
		return isInput;
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
				&& Objects.equals(location, other.location)
				&& isInput == other.isInput;
	}

	@Override
	public int hashCode() {
		return location == null
			? flow.hashCode()
			: flow.hashCode() * 31 + location.hashCode();
	}
}
