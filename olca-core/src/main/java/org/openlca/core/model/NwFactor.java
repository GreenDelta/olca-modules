package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_nw_factors")
public class NwFactor extends AbstractEntity implements Cloneable {

	@OneToOne
	@JoinColumn(name = "f_impact_category")
	private ImpactCategory impactCategory;

	@Column(name = "normalisation_factor")
	private Double normalisationFactor;

	@Column(name = "weighting_factor")
	private Double weightingFactor;

	@Override
	protected NwFactor clone() {
		NwFactor clone = new NwFactor();
		clone.setNormalisationFactor(getNormalisationFactor());
		clone.setImpactCategory(getImpactCategory());
		clone.setWeightingFactor(getWeightingFactor());
		return clone;
	}

	public Double getNormalisationFactor() {
		return normalisationFactor;
	}

	public Double getWeightingFactor() {
		return weightingFactor;
	}

	public void setNormalisationFactor(Double normalisationFactor) {
		this.normalisationFactor = normalisationFactor;
	}

	public void setWeightingFactor(Double weightingFactor) {
		this.weightingFactor = weightingFactor;
	}

	public ImpactCategory getImpactCategory() {
		return impactCategory;
	}

	public void setImpactCategory(ImpactCategory impactCategory) {
		this.impactCategory = impactCategory;
	}

}
