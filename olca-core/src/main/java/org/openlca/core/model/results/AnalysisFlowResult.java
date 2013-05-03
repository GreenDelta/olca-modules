package org.openlca.core.model.results;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;

public class AnalysisFlowResult {

	private double aggregatedResult;
	private Flow flow;
	private Process process;
	private double singleResult;

	public void setAggregatedResult(double aggregatedResult) {
		this.aggregatedResult = aggregatedResult;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public void setSingleResult(double singleResult) {
		this.singleResult = singleResult;
	}

	public double getAggregatedResult() {
		return aggregatedResult;
	}

	public Flow getFlow() {
		return flow;
	}

	public Process getProcess() {
		return process;
	}

	public double getSingleResult() {
		return singleResult;
	}

}
