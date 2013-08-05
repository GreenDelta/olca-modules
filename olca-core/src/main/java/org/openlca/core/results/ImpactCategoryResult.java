package org.openlca.core.results;

import org.openlca.core.model.AbstractEntity;

public class ImpactCategoryResult extends AbstractEntity {

	private long impactCategory;
	private String unit;
	private double value;
	private double normalizationFactor;
	private double weightingFactor;
	private String weightingUnit;

	public void setNormalizationFactor(double normalizationFactor) {
		this.normalizationFactor = normalizationFactor;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setWeightingFactor(double weightingFactor) {
		this.weightingFactor = weightingFactor;
	}

	public void setWeightingUnit(String weightingUnit) {
		this.weightingUnit = weightingUnit;
	}

	public double getNormalizedValue() {
		return value / normalizationFactor;
	}

	public String getUnit() {
		return unit;
	}

	public double getValue() {
		return value;
	}

	public double getWeightedValue() {
		return value / normalizationFactor * weightingFactor;
	}

	public String getWeightingUnit() {
		return weightingUnit;
	}
}
