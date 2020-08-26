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
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

	/**
	 * The original author of the method.
	 */
	@OneToOne
	@JoinColumn(name = "f_author")
	public Actor author;

	/**
	 * The person/organization that adapted/converted the method into this
	 * machine readable format.
	 */
	@OneToOne
	@JoinColumn(name = "f_generator")
	public Actor generator;

	@OneToMany
	@JoinTable(name = "tbl_source_links", joinColumns = {
			@JoinColumn(name = "f_owner") }, inverseJoinColumns = {
					@JoinColumn(name = "f_source") })
	public final List<Source> sources = new ArrayList<>();

	@Override
	public ImpactMethod clone() {
		ImpactMethod clone = new ImpactMethod();
		Util.copyRootFields(this, clone);
		clone.category = category;
		HashMap<ImpactCategory, ImpactCategory> impactMap = new HashMap<>();
		for (ImpactCategory origCat : impactCategories) {
			ImpactCategory clonedCat = origCat.clone();
			impactMap.put(origCat, clonedCat);
			clone.impactCategories.add(clonedCat);
		}
		for (Parameter p : parameters) {
			clone.parameters.add(p.clone());
		}
		clone.author = author;
		clone.generator = generator;
		for (Source source : sources) {
			clone.sources.add(source);
		}
		cloneNwSets(clone, impactMap);
		return clone;
	}

	private void cloneNwSets(ImpactMethod clone,
			HashMap<ImpactCategory, ImpactCategory> impactMap) {
		for (NwSet nwSet : nwSets) {
			NwSet clonedSet = nwSet.clone();
			clone.nwSets.add(clonedSet);
			for (NwFactor factor : clonedSet.factors) {
				ImpactCategory clonedCat = impactMap.get(factor.impactCategory);
				factor.impactCategory = clonedCat;
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
