package org.openlca.core.matrix;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

/**
 * Describes the mapping of flow information to a matrix index.
 */
public class IndexFlow {

	/**
	 * The matrix index to which this flow information is mapped.
	 */
	public int index;

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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		IndexFlow other = (IndexFlow) o;
		return index == other.index;
	}

	@Override
	public int hashCode() {
		return index;
	}
}
