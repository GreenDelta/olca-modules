package org.openlca.core.results;

import org.openlca.core.model.descriptors.FlowDescriptor;

public class SimpleFlowResult {

	private FlowDescriptor flow;
	private boolean input;
	private double value;

	public FlowDescriptor getFlow() {
		return flow;
	}

	public void setFlow(FlowDescriptor flow) {
		this.flow = flow;
	}

	public boolean isInput() {
		return input;
	}

	public void setInput(boolean input) {
		this.input = input;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
