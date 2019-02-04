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
	public ImpactCategory impactCategory;

	@Column(name = "normalisation_factor")
	public Double normalisationFactor;

	@Column(name = "weighting_factor")
	public Double weightingFactor;

	@Override
	protected NwFactor clone() {
		NwFactor clone = new NwFactor();
		clone.normalisationFactor = normalisationFactor;
		clone.impactCategory = impactCategory;
		clone.weightingFactor = weightingFactor;
		return clone;
	}

}
