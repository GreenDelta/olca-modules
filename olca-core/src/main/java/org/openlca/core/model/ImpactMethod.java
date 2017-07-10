package org.openlca.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_impact_methods")
public class ImpactMethod extends CategorizedEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	public final List<ImpactCategory> impactCategories = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	public final List<NwSet> nwSets = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<Parameter> parameters = new ArrayList<>();

	/**
	 * This field is used in the context of regionalized LCIA: when a process
	 * geography covers multiple shapes in a parameter shapefile the parameter
	 * values from these shapes that are used to calculate the characterization
	 * factors can be aggregated using different functions, e.g. via a weighted
	 * mean using the size of the intersections of the process geography with
	 * the parameter shapes, a simple arithmetic mean, etc.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "parameter_mean")
	public ParameterMean parameterMean;

	@Override
	public ImpactMethod clone() {
		ImpactMethod clone = new ImpactMethod();
		Util.cloneRootFields(this, clone);
		clone.setCategory(getCategory());
		HashMap<ImpactCategory, ImpactCategory> impactMap = new HashMap<>();
		for (ImpactCategory origCat : impactCategories) {
			ImpactCategory clonedCat = origCat.clone();
			impactMap.put(origCat, clonedCat);
			clone.impactCategories.add(clonedCat);
		}
		for (Parameter parameter : parameters)
			clone.parameters.add(parameter.clone());
		cloneNwSets(clone, impactMap);
		return clone;
	}

	private void cloneNwSets(ImpactMethod clone,
			HashMap<ImpactCategory, ImpactCategory> impactMap) {
		for (NwSet nwSet : nwSets) {
			NwSet clonedSet = nwSet.clone();
			clone.nwSets.add(clonedSet);
			for (NwFactor factor : clonedSet.factors) {
				ImpactCategory clonedCat = impactMap.get(factor
						.getImpactCategory());
				factor.setImpactCategory(clonedCat);
			}
		}
	}

	/**
	 * See the field ImpactMethod.parameterMean for more information.
	 */
	public static enum ParameterMean {

		ARITHMETIC_MEAN,

		WEIGHTED_MEAN,

	}
}