package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_normalization_weighting_factors")
public class NormalizationWeightingFactor extends AbstractEntity {

	@Column(name = "f_impact_category")
	private long impactCategoryId;

	@Column(name = "normalizationfactor")
	private Double normalizationFactor;

	@Column(name = "weightingfactor")
	private Double weightingFactor;

	public Double getNormalizationFactor() {
		return normalizationFactor;
	}

	public Double getWeightingFactor() {
		return weightingFactor;
	}

	public void setNormalizationFactor(Double normalizationFactor) {
		this.normalizationFactor = normalizationFactor;
	}

	public void setWeightingFactor(Double weightingFactor) {
		this.weightingFactor = weightingFactor;
	}

	public void setImpactCategoryId(long impactCategoryId) {
		this.impactCategoryId = impactCategoryId;
	}

	public long getImpactCategoryId() {
		return impactCategoryId;
	}

}
