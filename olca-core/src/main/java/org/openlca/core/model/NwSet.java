package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * Normalization and weighting set.
 */
@Entity
@Table(name = "tbl_nw_sets")
public class NwSet extends RefEntity {

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

	public NwSet add(NwFactor factor) {
		if (factor == null)
			return this;
		factors.add(factor);
		return this;
	}

	@Override
	public NwSet copy() {
		var clone = new NwSet();
		Entities.copyRefFields(this, clone);
		clone.weightedScoreUnit = weightedScoreUnit;
		for (var factor : factors) {
			clone.factors.add(factor.copy());
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
