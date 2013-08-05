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

	private FlowDescriptor flow;
	private ProcessDescriptor process;
	private double totalResult;
	private double singleResult;

	public void setTotalResult(double totalResult) {
		this.totalResult = totalResult;
	}

	public void setFlow(FlowDescriptor flow) {
		this.flow = flow;
	}

	public void setProcess(ProcessDescriptor process) {
		this.process = process;
	}

	public void setSingleResult(double singleResult) {
		this.singleResult = singleResult;
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
