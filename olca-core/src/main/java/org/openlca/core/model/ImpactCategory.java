package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_impact_categories")
public class ImpactCategory extends RootEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_category")
	private final List<ImpactFactor> impactFactors = new ArrayList<>();

	@Column(name = "reference_unit")
	private String referenceUnit;

	@Override
	public ImpactCategory clone() {
		final ImpactCategory lciaCategory = new ImpactCategory();
		lciaCategory.setRefId(UUID.randomUUID().toString());
		lciaCategory.setDescription(getDescription());
		lciaCategory.setName(getName());
		lciaCategory.setReferenceUnit(getReferenceUnit());
		for (ImpactFactor lciaFactor : getImpactFactors())
			lciaCategory.getImpactFactors().add(lciaFactor.clone());
		return lciaCategory;
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public void setReferenceUnit(String referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

	public List<ImpactFactor> getImpactFactors() {
		return impactFactors;
	}

}
