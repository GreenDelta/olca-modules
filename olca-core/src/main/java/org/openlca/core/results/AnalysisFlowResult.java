package org.openlca.core.results;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A flow result of a process in a product system. The result contains a value
 * of the single contribution of the process to this flow (singleResult) and the
 * upstream-total result (totalResult). The values are given in the reference
 * unit and flow property of the flow.
 */
public class AnalysisFlowResult {

	private boolean input;
	private FlowDescriptor flow;
	private ProcessDescriptor process;
	private double totalResult;
	private double singleResult;

	void setInput(boolean input) {
		this.input = input;
	}

	void setTotalResult(double totalResult) {
		this.totalResult = totalResult;
	}

	void setFlow(FlowDescriptor flow) {
		this.flow = flow;
	}

	void setProcess(ProcessDescriptor process) {
		this.process = process;
	}

	void setSingleResult(double singleResult) {
		this.singleResult = singleResult;
	}

	public boolean isInput() {
		return input;
	}

	public double getTotalResult() {
		return totalResult;
	}

	public FlowDescriptor getFlow() {
		return flow;
	}

	public ProcessDescriptor getProcess() {
		return process;
	}

	public double getSingleResult() {
		return singleResult;
	}

}
