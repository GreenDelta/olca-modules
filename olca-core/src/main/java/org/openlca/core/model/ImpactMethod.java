package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * An impact assessment method.
 */
@Entity
@Table(name = "tbl_impact_methods")
public class ImpactMethod extends CategorizedEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	private final List<ImpactCategory> impactCategories = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	private final List<NormalizationWeightingSet> normalizationWeightingSets = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<Parameter> parameters = new ArrayList<>();

	@Override
	public ImpactMethod clone() {
		ImpactMethod lciaMethod = new ImpactMethod();
		lciaMethod.setRefId(UUID.randomUUID().toString());
		lciaMethod.setName(getName());
		lciaMethod.setCategory(getCategory());
		lciaMethod.setDescription(getDescription());
		for (ImpactCategory lciaCategory : getImpactCategories()) {
			lciaMethod.getImpactCategories().add(lciaCategory.clone());
		}
		// TODO: clone parameters and nw-sets!
		return lciaMethod;
	}

	public List<ImpactCategory> getImpactCategories() {
		return impactCategories;
	}

	public List<NormalizationWeightingSet> getNormalizationWeightingSets() {
		return normalizationWeightingSets;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

}