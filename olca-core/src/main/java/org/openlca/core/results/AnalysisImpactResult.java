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

	private double totalResult;
	private double singleResult;
	private double normalizationFactor = 1;
	private double weightingFactor = 1;
	private String weightingUnit;

	void setImpactCategory(ImpactCategoryDescriptor impactCategory) {
		this.impactCategory = impactCategory;
	}

	void setProcess(ProcessDescriptor process) {
		this.process = process;
	}

	void setTotalResult(double totalResult) {
		this.totalResult = totalResult;
	}

	void setSingleResult(double singleResult) {
		this.singleResult = singleResult;
	}

	void setNormalizationFactor(double normalizationFactor) {
		this.normalizationFactor = normalizationFactor;
	}

	void setWeightingFactor(double weightingFactor) {
		this.weightingFactor = weightingFactor;
	}

	void setWeightingUnit(String weightingUnit) {
		this.weightingUnit = weightingUnit;
	}

	public ImpactCategoryDescriptor getImpactCategory() {
		return impactCategory;
	}

	public ProcessDescriptor getProcess() {
		return process;
	}

	public double getTotalResult() {
		return totalResult;
	}

	public double getSingleResult() {
		return singleResult;
	}

	public String getWeightingUnit() {
		return weightingUnit;
	}

	public double getNormalizedTotalResult() {
		return totalResult / normalizationFactor;
	}

	public double getNormalizedSingleResult() {
		return singleResult / normalizationFactor;
	}

	public double getWeightedTotalResult() {
		return getNormalizedTotalResult() * weightingFactor;
	}

	public double getWeightedSingleResult() {
		return getNormalizedSingleResult() * weightingFactor;
	}

}
