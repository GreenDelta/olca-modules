package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
public class UnitGroup extends RootEntity {

	@OneToOne
	@JoinColumn(name = "f_default_flow_property")
	public FlowProperty defaultFlowProperty;

	@OneToOne
	@JoinColumn(name = "f_reference_unit")
	public Unit referenceUnit;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_unit_group")
	public final List<Unit> units = new ArrayList<>();

	public static UnitGroup of(String name) {
		var group = new UnitGroup();
		Entities.init(group, name);
		return group;
	}

	public static UnitGroup of(String name, String refUnit) {
		return of(name, Unit.of(refUnit));
	}

	/**
	 * Creates a new unit group with the given name and reference unit.
	 */
	public static UnitGroup of(String name, Unit refUnit) {
		var group = new UnitGroup();
		Entities.init(group, name);
		group.units.add(refUnit);
		group.referenceUnit = refUnit;
		return group;
	}

	@Override
	public UnitGroup copy() {
		var clone = new UnitGroup();
		Entities.copyFields(this, clone);
		clone.defaultFlowProperty = defaultFlowProperty;
		for (Unit unit : units) {
			Unit copy = unit.copy();
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
}
