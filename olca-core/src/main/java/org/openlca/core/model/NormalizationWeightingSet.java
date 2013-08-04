package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

@Entity
@Table(name = "tbl_normalisation_weighting_sets")
public class NormalizationWeightingSet extends AbstractEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_normalisation_weighting_set")
	private final List<NormalizationWeightingFactor> normalizationWeightingFactors = new ArrayList<>();

	@Column(name = "reference_system")
	private String referenceSystem;

	@Column(name = "unit")
	private String unit;

	public NormalizationWeightingSet() {
	}

	public NormalizationWeightingFactor getFactor(ImpactCategory category) {
		if (category == null)
			return null;
		return getFactor(category.getId());
	}

	public NormalizationWeightingFactor getFactor(
			ImpactCategoryDescriptor descriptor) {
		if (descriptor == null)
			return null;
		return getFactor(descriptor.getId());
	}

	private NormalizationWeightingFactor getFactor(long categoryId) {
		for (NormalizationWeightingFactor fac : normalizationWeightingFactors) {
			if (categoryId == fac.getImpactCategoryId())
				return fac;
		}
		return null;
	}

	public List<NormalizationWeightingFactor> getNormalizationWeightingFactors() {
		return normalizationWeightingFactors;
	}

	public String getReferenceSystem() {
		return referenceSystem;
	}

	public String getUnit() {
		return unit;
	}

	public void setReferenceSystem(final String referenceSystem) {
		this.referenceSystem = referenceSystem;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

}
