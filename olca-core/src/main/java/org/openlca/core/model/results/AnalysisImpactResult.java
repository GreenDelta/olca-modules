package org.openlca.core.model.results;

import org.openlca.core.model.Process;

/**
 * Aggregated and single result of a process for a specific LCIA category
 * 
 * @author Sebastian Greve
 * 
 */
public class AnalysisImpactResult {

	private LCIACategoryResult aggregatedResult;
	private String category;
	private Process process;
	private LCIACategoryResult singleResult;

	public LCIACategoryResult getAggregatedResult() {
		return aggregatedResult;
	}

	public String getCategory() {
		return category;
	}

	public Process getProcess() {
		return process;
	}

	public LCIACategoryResult getSingleResult() {
		return singleResult;
	}

	public void setAggregatedResult(LCIACategoryResult aggregatedResult) {
		this.aggregatedResult = aggregatedResult;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public void setSingleResult(LCIACategoryResult singleResult) {
		this.singleResult = singleResult;
	}

}
