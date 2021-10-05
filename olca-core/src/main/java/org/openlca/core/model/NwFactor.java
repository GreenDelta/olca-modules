package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_nw_factors")
public class NwFactor extends AbstractEntity implements Copyable<NwFactor> {

	@OneToOne
	@JoinColumn(name = "f_impact_category")
	public ImpactCategory impactCategory;

	@Column(name = "normalisation_factor")
	public Double normalisationFactor;

	@Column(name = "weighting_factor")
	public Double weightingFactor;

	public NwFactor() {
	}

	private NwFactor(ImpactCategory impact,Double nFactor, Double wFactor) {
		this.impactCategory = impact;
		this.normalisationFactor = nFactor;
		this.weightingFactor = wFactor;
	}

	public static NwFactor of(
			ImpactCategory impact,
			double normalisationFactor,
			double weightingFactor) {
		return new NwFactor(impact, normalisationFactor, weightingFactor);
	}

	public static NwFactor ofNormalization(ImpactCategory impact, double factor) {
		return new NwFactor(impact, factor, null);
	}

	public static NwFactor ofWeighting(ImpactCategory impact, double factor) {
		return new NwFactor(impact, null, factor);
	}

	@Override
	public NwFactor copy() {
		var clone = new NwFactor();
		clone.normalisationFactor = normalisationFactor;
		clone.impactCategory = impactCategory;
		clone.weightingFactor = weightingFactor;
		return clone;
	}

}
