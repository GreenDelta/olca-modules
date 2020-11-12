package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * Normalization and weighting set.
 */
@Entity
@Table(name = "tbl_nw_sets")
public class NwSet extends RootEntity {

	@JoinColumn(name = "f_nw_set")
	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	public final List<NwFactor> factors = new ArrayList<>();

	@Column(name = "weighted_score_unit")
	public String weightedScoreUnit;

	public static NwSet of(String name) {
		var nwSet = new NwSet();
		Entities.init(nwSet, name);
		return nwSet;
	}

	@Override
	public NwSet clone() {
		var clone = new NwSet();
		Entities.copyRootFields(this, clone);
		clone.weightedScoreUnit = weightedScoreUnit;
		for (NwFactor factor : factors) {
			clone.factors.add(factor.clone());
		}
		return clone;
	}

	public NwFactor getFactor(ImpactCategory category) {
		for (NwFactor fac : factors)
			if (Objects.equals(category, fac.impactCategory))
				return fac;
		return null;
	}

	public NwFactor getFactor(ImpactDescriptor category) {
		for (NwFactor fac : factors)
			if (category.id == fac.impactCategory.id)
				return fac;
		return null;
	}
}
