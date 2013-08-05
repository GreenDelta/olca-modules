package org.openlca.core.results;

import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * An impact category result of a process in a product system. The result
 * contains a value of the single contribution of the process to this impact
 * category (singleResult) and the upstream-total result (totalResult).
 */
public class AnalysisImpactResult {

	private ImpactCategoryDescriptor impactCategory;
	private ProcessDescriptor process;
	private ImpactCategoryResult totalResult;
	private ImpactCategoryResult singleResult;

	public ImpactCategoryResult getTotalResult() {
		return totalResult;
	}

	public ImpactCategoryDescriptor getImpactCategory() {
		return impactCategory;
	}

	public ProcessDescriptor getProcess() {
		return process;
	}

	public ImpactCategoryResult getSingleResult() {
		return singleResult;
	}

	public void setTotalResult(ImpactCategoryResult totalResult) {
		this.totalResult = totalResult;
	}

	public void setImpactCategory(ImpactCategoryDescriptor impactCategory) {
		this.impactCategory = impactCategory;
	}

	public void setProcess(ProcessDescriptor process) {
		this.process = process;
	}

	public void setSingleResult(ImpactCategoryResult singleResult) {
		this.singleResult = singleResult;
	}

}
