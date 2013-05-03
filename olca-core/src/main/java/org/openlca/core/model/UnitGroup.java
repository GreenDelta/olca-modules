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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IModelComponent;

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
public class UnitGroup extends AbstractEntity implements IModelComponent,
		PropertyChangeListener, Copyable<UnitGroup>,
		IdentifyableByVersionAndUUID {

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_defaultflowproperty")
	private FlowProperty defaultFlowProperty;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "name")
	private String name;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_referenceunit")
	private Unit referenceUnit;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_unitgroup")
	private final List<Unit> units = new ArrayList<>();

	public UnitGroup() {
	}

	public UnitGroup(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	/**
	 * Initializes the property change listener after object is loaded from
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		for (final Unit unit : getUnits()) {
			unit.addPropertyChangeListener(this);
		}
	}

	public void add(final Unit unit) {
		if (!units.contains(unit)) {
			units.add(unit);
			support.firePropertyChange("units", null, unit);
			unit.addPropertyChangeListener(this);
		}
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public UnitGroup copy() {
		final UnitGroup unitGroup = new UnitGroup(UUID.randomUUID().toString(),
				getName());
		unitGroup.setCategoryId(getCategoryId());
		unitGroup.setDefaultFlowProperty(getDefaultFlowProperty());
		for (final Unit unit : getUnits()) {
			final boolean isRef = getReferenceUnit().getId().equals(
					unit.getId());
			final Unit copy = unit.copy();
			unitGroup.add(copy);
			if (isRef) {
				unitGroup.setReferenceUnit(copy);
			}
		}
		return unitGroup;
	}

	public FlowProperty getDefaultFlowProperty() {
		return defaultFlowProperty;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getUUID() {
		return getId();
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getName() {
		return name;
	}

	public Unit getReferenceUnit() {
		return referenceUnit;
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

	public Unit[] getUnits() {
		return units.toArray(new Unit[units.size()]);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	public void remove(final Unit unit) {
		units.remove(unit);
		unit.removePropertyChangeListener(this);
		support.firePropertyChange("units", unit, null);
	}

	@Override
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	@Override
	public void setCategoryId(final String categoryId) {
		support.firePropertyChange("categoryId", this.categoryId,
				this.categoryId = categoryId);
	}

	public void setDefaultFlowProperty(final FlowProperty defaultFlowProperty) {
		support.firePropertyChange("defaultFlowProperty",
				this.defaultFlowProperty,
				this.defaultFlowProperty = defaultFlowProperty);
	}

	@Override
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	public void setReferenceUnit(final Unit referenceUnit) {
		support.firePropertyChange("referenceUnit", this.referenceUnit,
				this.referenceUnit = referenceUnit);
	}

}
