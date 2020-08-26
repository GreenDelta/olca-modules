package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A set of {@link Unit} objects which are directly convertible into each other
 * (e.g. units of mass: kg, g, mg...). A unit set has a reference unit with the
 * conversion factor 1.0. The respective conversion factor of the other units is
 * defined by the equation: f = [uRef] / [u] (with f: the conversion factor of
 * the respective unit, [uRef] the equivalent amount in the reference unit, [u]
 * the equivalent amount in the respective unit; e.g. f(kg) = 1.0 -&gt; f(g) =
 * 0.001).
 */
@Entity
@Table(name = "tbl_unit_groups")
public class UnitGroup extends CategorizedEntity {

	@OneToOne
	@JoinColumn(name = "f_default_flow_property")
	public FlowProperty defaultFlowProperty;

	@OneToOne
	@JoinColumn(name = "f_reference_unit")
	public Unit referenceUnit;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_unit_group")
	public final List<Unit> units = new ArrayList<>();

	@Override
	public UnitGroup clone() {
		UnitGroup clone = new UnitGroup();
		Util.copyRootFields(this, clone);
		clone.category = category;
		clone.defaultFlowProperty = defaultFlowProperty;
		Unit refUnit = referenceUnit;
		for (Unit unit : units) {
			final boolean isRef = Objects.equals(refUnit, unit);
			final Unit copy = unit.clone();
			clone.units.add(copy);
			if (isRef) {
				clone.referenceUnit = copy;
			}
		}
		return clone;
	}

	/**
	 * Returns the unit with the specified name or synonym from this group.
	 */
	public Unit getUnit(String name) {
		for (Unit unit : units) {
			if (unit.name.equals(name))
				return unit;
			String synonyms = unit.synonyms;
			if (synonyms == null)
				continue;
			for (String syn : synonyms.split(";")) {
				if (syn.trim().equals(name.trim()))
					return unit;
			}
		}
		return null;
	}

}
