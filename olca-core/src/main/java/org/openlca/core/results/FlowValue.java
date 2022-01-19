package org.openlca.core.results;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class FlowValue {

	public FlowDescriptor flow;

	/**
	 * Contains a location in case of a regionalized flow result. Otherwise, this
	 * field may be zero
	 */
	public LocationDescriptor location;

	public boolean input;
	public double value;

	public FlowValue() {
	}

	public FlowValue(EnviFlow flow, double value) {
		this.flow = flow.flow();
		this.location = flow.location();
		this.input = flow.isInput();
		this.value = value;
	}



}
