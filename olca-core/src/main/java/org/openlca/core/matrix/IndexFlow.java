package org.openlca.core.matrix;

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
	public FlowDescriptor flow;

	/**
	 * In case of a regionalized flow index flow-location pairs are mapped to matrix
	 * indices.
	 */
	public LocationDescriptor location;

	/**
	 * Indicates whether the flow is an input flow or not.
	 */
	public boolean isInput;

	public static IndexFlow ofInput(FlowDescriptor flow) {
		var iflow = new IndexFlow();
		iflow.flow = flow;
		iflow.isInput = true;
		return iflow;
	}

	public static IndexFlow ofInput(FlowDescriptor flow, LocationDescriptor loc) {
		var iflow = new IndexFlow();
		iflow.flow = flow;
		iflow.location = loc;
		iflow.isInput = true;
		return iflow;
	}

	public static IndexFlow ofOutput(FlowDescriptor flow) {
		var iflow = new IndexFlow();
		iflow.flow = flow;
		iflow.isInput = false;
		return iflow;
	}

	public static IndexFlow ofOutput(FlowDescriptor flow, LocationDescriptor loc) {
		var iflow = new IndexFlow();
		iflow.flow = flow;
		iflow.location = loc;
		iflow.isInput = false;
		return iflow;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		IndexFlow other = (IndexFlow) o;
		return Objects.equals(flow, other.flow)
				&& Objects.equals(location, other.location);
	}

	@Override
	public int hashCode() {
		if (flow == null)
			return super.hashCode();
		if (location == null)
			return (int) flow.id;
		return LongPair.hash(flow.id, location.id);
	}
}
