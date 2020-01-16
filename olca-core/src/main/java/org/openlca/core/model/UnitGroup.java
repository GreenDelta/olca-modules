package org.openlca.core.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A set of {@link Unit} objects which are directly convertible into each other
 * (e.g. units of mass: kg, g, mg...). A unit group has a reference unit with the
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
		Util.cloneRootFields(this, clone);
		clone.category = category;
		clone.defaultFlowProperty = defaultFlowProperty;
		for (Unit unit : units) {
			Unit copy = unit.clone();
			clone.units.add(copy);
			if (Objects.equals(referenceUnit, unit)) {
				clone.referenceUnit = copy;
			}
		}
		return clone;
	}

	/**
	 * Returns the unit with the specified name or synonym from this group.
	 */
	public Unit getUnit(String name) {
		// first we only search in the name fields because this is faster
		for (Unit u : units) {
			if (Objects.equals(u.name, name))
				return u;
		}
		// then we search in synonyms
		if (name == null)
			return null;
		for (Unit u : units) {
			String synonyms = u.synonyms;
			if (synonyms == null)
				continue;
			for (String syn : synonyms.split(";")) {
				if (syn.trim().equals(name))
					return u;
			}
		}
		return null;
	}

	/**
	 * Creates a unit with the given name and adds it as reference unit (with
	 * a conversion factor of 1.0) to this unit group.
	 */
	public Unit addReferenceUnit(String name) {
		Unit unit = addUnit(name, 1.0);
		referenceUnit = unit;
		return unit;
	}

	/**
	 * Creates a unit with the given name and adds it with the given conversion
	 * factor to this unit group.
	 */
	public Unit addUnit(String name, double conversionFactor) {
		Unit unit = new Unit();
		unit.name = name;
		unit.conversionFactor = conversionFactor;
		units.add(unit);
		return unit;
	}

}
