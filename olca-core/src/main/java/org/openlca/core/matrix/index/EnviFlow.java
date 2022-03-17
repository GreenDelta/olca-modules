package org.openlca.core.matrix.index;

import java.util.Objects;

import org.openlca.core.model.AbstractExchange;
import org.openlca.core.model.descriptors.RootDescriptor;
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
 *                 {@code false}
 * @param wrapped  if this flow is a virtual flow, this field contains the
 *                 descriptor of the wrapped content of this flow.
 */
public record EnviFlow(
	FlowDescriptor flow,
	LocationDescriptor location,
	boolean isInput,
	Descriptor wrapped) {

	public EnviFlow(
		FlowDescriptor flow,
		LocationDescriptor location,
		boolean isInput,
		Descriptor wrapped) {
		this.flow = Objects.requireNonNull(flow);
		this.location = location;
		this.isInput = isInput;
		this.wrapped = wrapped;
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
		return inputOf(flow, null);
	}

	public static EnviFlow inputOf(FlowDescriptor flow, LocationDescriptor loc) {
		return new EnviFlow(flow, loc, true, null);
	}

	public static EnviFlow outputOf(FlowDescriptor flow) {
		return outputOf(flow, null);
	}

	public static EnviFlow outputOf(FlowDescriptor flow, LocationDescriptor loc) {
		return new EnviFlow(flow, loc, false, null);
	}

	/**
	 * Creates a virtual flow for the given descriptor. The descriptor is
	 * converted to a FlowDescriptor with the ID of that descriptor. Thus, it
	 * is important, that this ID is not used for any other flow. Virtual flows
	 * are added to the inventory to include values in the calculation just like
	 * for normal flows. This can be the case for calculations with LCIA results
	 * or social indicators.
	 *
	 * @param d the descriptor that should be wrapped as a virtual flow
	 * @return a new instance that is tagged as virtual flow
	 */
	public static EnviFlow virtualOf(Descriptor d) {
		FlowDescriptor flow;
		if (d instanceof FlowDescriptor fd) {
			flow = fd;
		} else {
			flow = new FlowDescriptor();
			flow.id = d.id;
			flow.name = d.name;
			flow.description = d.description;
			flow.refId = d.refId;
			if (d instanceof RootDescriptor cd) {
				flow.category = cd.category;
			}
		}
		return new EnviFlow(flow, null, false, d);
	}

	public boolean isVirtual() {
		return wrapped != null;
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
