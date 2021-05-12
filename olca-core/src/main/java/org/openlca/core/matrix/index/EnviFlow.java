package org.openlca.core.matrix.index;

import java.util.Objects;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * Describes the mapping of flow information to a matrix index.
 */
public class EnviFlow {

	private final FlowDescriptor flow;
	private final LocationDescriptor location;
	private final boolean isInput;

	EnviFlow(FlowDescriptor flow, LocationDescriptor location, boolean isInput) {
		this.flow = Objects.requireNonNull(flow);
		this.location = location;
		this.isInput = isInput;
	}

	public static EnviFlow inputOf(FlowDescriptor flow) {
		return new EnviFlow(flow, null, true);
	}

	public static EnviFlow inputOf(FlowDescriptor flow, LocationDescriptor loc) {
		return new EnviFlow(flow, loc, true);
	}

	public static EnviFlow outputOf(FlowDescriptor flow) {
		return new EnviFlow(flow, null, false);
	}

	public static EnviFlow outputOf(FlowDescriptor flow, LocationDescriptor loc) {
		return new EnviFlow(flow, loc, false);
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
		var other = (EnviFlow) o;
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
