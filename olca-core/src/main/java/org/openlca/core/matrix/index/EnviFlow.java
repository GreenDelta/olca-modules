package org.openlca.core.matrix.index;

import java.util.Objects;

import org.openlca.core.model.AbstractExchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * Describes the mapping of flow information to a matrix index.
 *
 * @param flow     the flow descriptor which is never {@code null}
 * @param location the location descriptor which is {@code null} for
 *                 non-regionalized indices
 * @param isInput  {@code true} when this flow is an input flow, otherwise
 *                 * {@code false}
 */
public record EnviFlow(
	FlowDescriptor flow, LocationDescriptor location, boolean isInput) {

	public EnviFlow(
		FlowDescriptor flow, LocationDescriptor location, boolean isInput) {
		this.flow = Objects.requireNonNull(flow);
		this.location = location;
		this.isInput = isInput;
	}

	public static EnviFlow of(AbstractExchange e) {
		if (e == null || e.flow == null)
			return null;
		var flow = Descriptor.of(e.flow);
		if (e.location == null)
			return e.isInput ? inputOf(flow) : outputOf(flow);
		var location = Descriptor.of(e.location);
		return e.isInput
			? inputOf(flow, location)
			: outputOf(flow, location);
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
