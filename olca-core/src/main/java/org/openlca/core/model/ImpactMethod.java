package org.openlca.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_impact_methods")
public class ImpactMethod extends CategorizedEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	private final List<ImpactCategory> impactCategories = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	private final List<NwSet> nwSets = new ArrayList<>();

	@Override
	public ImpactMethod clone() {
		ImpactMethod clone = new ImpactMethod();
		clone.setRefId(UUID.randomUUID().toString());
		clone.setName(getName());
		clone.setCategory(getCategory());
		clone.setDescription(getDescription());
		HashMap<ImpactCategory, ImpactCategory> impactMap = new HashMap<>();
		for (ImpactCategory origCat : getImpactCategories()) {
			ImpactCategory clonedCat = origCat.clone();
			impactMap.put(origCat, clonedCat);
			clone.getImpactCategories().add(clonedCat);
		}
		cloneNwSets(clone, impactMap);
		return clone;
	}

	private void cloneNwSets(ImpactMethod clone,
	                         HashMap<ImpactCategory, ImpactCategory> impactMap) {
		for(NwSet nwSet : getNwSets()) {
			NwSet clonedSet = nwSet.clone();
			clone.getNwSets().add(clonedSet);
			for(NwFactor factor : nwSet.getFactors()) {
				ImpactCategory clonedCat = impactMap.get(factor
						.getImpactCategory());
				factor.setImpactCategory(clonedCat);
			}
		}
	}

	public List<ImpactCategory> getImpactCategories() {
		return impactCategories;
	}

	public List<NwSet> getNwSets() {
		return nwSets;
	}

}