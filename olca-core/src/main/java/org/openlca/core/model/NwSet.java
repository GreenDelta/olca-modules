package org.openlca.core.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Normalisation and weighting set.
 */
@Entity
@Table(name = "tbl_nw_sets")
public class NwSet extends RootEntity {

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "f_nw_set")
	private final List<NwFactor> factors = new ArrayList<>();

	@Column(name = "weighted_score_unit")
	private String weightedScoreUnit;

	@Override
	public NwSet clone() {
		NwSet clone = new NwSet();
		clone.setName(getName());
		clone.setDescription(getDescription());
		clone.setRefId(UUID.randomUUID().toString());
		clone.setWeightedScoreUnit(getWeightedScoreUnit());
		for(NwFactor factor : getFactors())
			clone.getFactors().add(factor.clone());
		return clone;
	}

	public NwFactor getFactor(ImpactCategory category) {
		for (NwFactor fac : factors) {
			if (Objects.equals(category, fac.getImpactCategory()))
				return fac;
		}
		return null;
	}

	public List<NwFactor> getFactors() {
		return factors;
	}

	public String getWeightedScoreUnit() {
		return weightedScoreUnit;
	}

	public void setWeightedScoreUnit(final String weightedScoreUnit) {
		this.weightedScoreUnit = weightedScoreUnit;
	}
}
