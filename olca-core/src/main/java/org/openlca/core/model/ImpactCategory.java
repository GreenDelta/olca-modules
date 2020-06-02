package org.openlca.core.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_impact_categories")
public class ImpactCategory extends CategorizedEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_category")
	public final List<ImpactFactor> impactFactors = new ArrayList<>();

	@Column(name = "reference_unit")
	public String referenceUnit;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<Parameter> parameters = new ArrayList<>();

	/**
	 * This field is used in the context of regionalized LCIA: when a process
	 * geography covers multiple shapes in a parameter shapefile the parameter
	 * values from these shapes that are used to calculate the characterization
	 * factors can be aggregated using different functions, e.g. via a weighted mean
	 * using the size of the intersections of the process geography with the
	 * parameter shapes, a simple arithmetic mean, etc.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "parameter_mean")
	public ParameterMean parameterMean;

	@Override
	public ImpactCategory clone() {
		ImpactCategory clone = new ImpactCategory();
		Util.cloneRootFields(this, clone);
		clone.category = category;
		clone.referenceUnit = referenceUnit;
		for (ImpactFactor f : impactFactors) {
			clone.impactFactors.add(f.clone());
		}
		for (Parameter p : parameters) {
			clone.parameters.add(p.clone());
		}
		clone.parameterMean = parameterMean;
		return clone;
	}

	public ImpactFactor getFactor(Flow flow) {
		if (flow == null)
			return null;
		for (ImpactFactor factor : impactFactors)
			if (flow.equals(factor.flow))
				return factor;
		return null;
	}

	public ImpactFactor getFactor(String refId) {
		if (refId == null)
			return null;
		for (ImpactFactor factor : impactFactors)
			if (factor.flow != null && refId.equals(factor.flow.refId))
				return factor;
		return null;
	}

	/**
	 * Adds a new characterization factor for the given flow initialized with a
	 * value of 1.0. The unit and flow property are initialized with the
	 * respective reference values of the flow.
	 */
	public ImpactFactor addFactor(Flow flow) {
		ImpactFactor f = new ImpactFactor();
		impactFactors.add(f);
		f.value = 1.0;
		if (flow == null)
			return f;
		f.flow = flow;
		f.flowPropertyFactor = flow.getReferenceFactor();
		f.unit = flow.getReferenceUnit();
		return f;
	}

	/**
	 * See the field ImpactMethod.parameterMean for more information.
	 */
	public enum ParameterMean {

		ARITHMETIC_MEAN,

		WEIGHTED_MEAN,

	}

}
