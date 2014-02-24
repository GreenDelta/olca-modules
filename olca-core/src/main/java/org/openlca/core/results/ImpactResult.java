package org.openlca.core.results;

import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactResult {

	private ImpactCategoryDescriptor impactCategory;
	private double value;
	private double normalizationFactor = 1;
	private double weightingFactor = 1;

	void setImpactCategory(ImpactCategoryDescriptor impactCategory) {
		this.impactCategory = impactCategory;
	}

	void setNormalizationFactor(double normalizationFactor) {
		this.normalizationFactor = normalizationFactor;
	}

	void setValue(double value) {
		this.value = value;
	}

	void setWeightingFactor(double weightingFactor) {
		this.weightingFactor = weightingFactor;
	}

	public ImpactCategoryDescriptor getImpactCategory() {
		return impactCategory;
	}

	public double getValue() {
		return value;
	}

	public double getNormalizedValue() {
		return value / normalizationFactor;
	}

	public double getWeightedValue() {
		return value / normalizationFactor * weightingFactor;
	}

}
