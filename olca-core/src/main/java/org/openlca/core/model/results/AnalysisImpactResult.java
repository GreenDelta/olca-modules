package org.openlca.core.model.results;

import org.openlca.core.model.Process;

/**
 * Aggregated and single result of a process for a specific LCIA category
 */
public class AnalysisImpactResult {

	private ImpactCategoryResult aggregatedResult;
	private String category;
	private Process process;
	private ImpactCategoryResult singleResult;

	public ImpactCategoryResult getAggregatedResult() {
		return aggregatedResult;
	}

	public String getCategory() {
		return category;
	}

	public Process getProcess() {
		return process;
	}

	public ImpactCategoryResult getSingleResult() {
		return singleResult;
	}

	public void setAggregatedResult(ImpactCategoryResult aggregatedResult) {
		this.aggregatedResult = aggregatedResult;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public void setSingleResult(ImpactCategoryResult singleResult) {
		this.singleResult = singleResult;
	}

}
