/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
@Table(name = "tbl_unitgroups")
public class UnitGroup extends RootEntity {

	@OneToOne
	@JoinColumn(name = "f_default_flow_property")
	private FlowProperty defaultFlowProperty;

	@OneToOne
	@JoinColumn(name = "f_reference_unit")
	private Unit referenceUnit;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_unit_group")
	private final List<Unit> units = new ArrayList<>();

	@Override
	public UnitGroup clone() {
		final UnitGroup unitGroup = new UnitGroup();
		unitGroup.setId(UUID.randomUUID().toString());
		unitGroup.setName(getName());
		unitGroup.setDescription(getDescription());
		unitGroup.setCategory(getCategory());
		unitGroup.setDefaultFlowProperty(getDefaultFlowProperty());
		for (final Unit unit : getUnits()) {
			final boolean isRef = getReferenceUnit().getId().equals(
					unit.getId());
			final Unit copy = unit.clone();
			unitGroup.getUnits().add(copy);
			if (isRef) {
				unitGroup.setReferenceUnit(copy);
			}
		}
		return unitGroup;
	}

	/**
	 * Returns the unit with the specified name or synonym from this group.
	 */
	public Unit getUnit(String name) {
		for (Unit unit : units) {
			if (unit.getName().equals(name))
				return unit;
			String synonyms = unit.getSynonyms();
			if (synonyms == null)
				continue;
			for (String syn : synonyms.split(";")) {
				if (syn.trim().equals(name.trim()))
					return unit;
			}
		}
		return null;
	}

	public FlowProperty getDefaultFlowProperty() {
		return defaultFlowProperty;
	}

	public void setDefaultFlowProperty(FlowProperty defaultFlowProperty) {
		this.defaultFlowProperty = defaultFlowProperty;
	}

	public Unit getReferenceUnit() {
		return referenceUnit;
	}

	public void setReferenceUnit(Unit referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

	public List<Unit> getUnits() {
		return units;
	}

}
