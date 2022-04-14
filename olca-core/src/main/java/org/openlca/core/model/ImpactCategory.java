package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_impact_categories")
public class ImpactCategory extends ParameterizedEntity {

	/**
	 * A code, short name, or abbreviation that identifies this impact category
	 * (like 'GWP' for global warming potential).
	 */
	@Column(name = "code")
	public String code;

	@Column(name = "reference_unit")
	public String referenceUnit;

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "f_impact_category")
	public final List<ImpactFactor> impactFactors = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_source")
	public Source source;

	/**
	 * The impact direction of this category. This field determines which signs
	 * the factors of this category will get in the characterization matrix. If
	 * this field is {@code null}, it will be inferred from the direction of the
	 * flows but this can lead to problems, e.g. when resources are used on the
	 * output side or emissions on the input side.
	 */
	@Column(name = "direction")
	@Enumerated(EnumType.STRING)
	public Direction direction;

	public static ImpactCategory of(String name) {
		return of(name, null);
	}

	public static ImpactCategory of(String name, String refUnit) {
		var impact = new ImpactCategory();
		Entities.init(impact, name);
		impact.referenceUnit = refUnit;
		return impact;
	}

	@Override
	public ImpactCategory copy() {
		var copy = new ImpactCategory();
		Entities.copyFields(this, copy);
		copy.direction = direction;
		copy.code = code;
		copy.referenceUnit = referenceUnit;
		copy.source = source;
		for (var f : impactFactors) {
			copy.impactFactors.add(f.copy());
		}
		for (var p : parameters) {
			copy.parameters.add(p.copy());
		}
		return copy;
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
	 * Adds a new characterization factor for the given flow and value. The unit
	 * and flow property are initialized with the respective reference values of
	 * the flow.
	 */
	public ImpactFactor factor(Flow flow, double value) {
		var f = ImpactFactor.of(flow, value);
		impactFactors.add(f);
		return f;
	}

	@Override
	public final ParameterScope parameterScope() {
		return ParameterScope.IMPACT;
	}
}
