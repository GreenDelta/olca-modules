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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * A {@link NormalizationWeightingSet} contains a
 * {@link NormalizationWeightingFactor} for each LCIA category of the LCIA
 * method it belongs to
 * 
 * @author Sebastian Greve
 * 
 */
@Entity
@Table(name = "tbl_normalisation_weighting_sets")
public class NormalizationWeightingSet extends AbstractEntity implements
		PropertyChangeListener {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_normalisation_weighting_set")
	private final List<NormalizationWeightingFactor> normalizationWeightingFactors = new ArrayList<>();

	@Column(name = "reference_system")
	private String referenceSystem;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Column(name = "unit")
	private String unit;

	public NormalizationWeightingSet() {

	}

	public NormalizationWeightingSet(String id, String referenceSystem,
			ImpactMethod method) {
		setId(id);
		this.referenceSystem = referenceSystem;
		if (method != null) {
			for (ImpactCategory category : method.getLCIACategories()) {
				NormalizationWeightingFactor fac = new NormalizationWeightingFactor();
				fac.setId(UUID.randomUUID().toString());
				fac.setImpactCategoryId(category.getId());
				normalizationWeightingFactors.add(fac);
			}
		}
	}

	/**
	 * Initializes the property change listener after object is loaded from
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		for (final NormalizationWeightingFactor factor : getNormalizationWeightingFactors()) {
			factor.addPropertyChangeListener(this);
		}
	}

	/**
	 * Adds a {@link NormalizationWeightingFactor}
	 * 
	 * @param normalizationWeightingFactor
	 *            The factor to add
	 */
	public void add(
			final NormalizationWeightingFactor normalizationWeightingFactor) {
		normalizationWeightingFactors.add(normalizationWeightingFactor);
		support.firePropertyChange("normalizationWeightingFactors", null,
				normalizationWeightingFactor);
	}

	/**
	 * Adds a property change listener
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public NormalizationWeightingFactor getNormalizationWeightingFactor(
			ImpactCategory category) {
		if (category == null)
			return null;
		return getFactor(category.getId());
	}

	public NormalizationWeightingFactor getFactor(
			ImpactCategoryDescriptor descriptor) {
		if (descriptor == null)
			return null;
		return getFactor(descriptor.getId());
	}

	private NormalizationWeightingFactor getFactor(String categoryId) {
		if (categoryId == null)
			return null;
		for (NormalizationWeightingFactor fac : normalizationWeightingFactors) {
			if (categoryId.equals(fac.getImpactCategoryId()))
				return fac;
		}
		return null;
	}

	/**
	 * Getter of the normalization/weighting factors
	 * 
	 * @return The normalization/weighting factors of the set
	 */
	public NormalizationWeightingFactor[] getNormalizationWeightingFactors() {
		return normalizationWeightingFactors
				.toArray(new NormalizationWeightingFactor[normalizationWeightingFactors
						.size()]);
	}

	/**
	 * Getter of the reference system
	 * 
	 * @return The reference system of the set
	 */
	public String getReferenceSystem() {
		return referenceSystem;
	}

	/**
	 * Getter of the unit
	 * 
	 * @return The weighting unit of the set
	 */
	public String getUnit() {
		return unit;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	/**
	 * Removes a {@link NormalizationWeightingFactor}
	 * 
	 * @param normalizationWeightingFactor
	 *            The factor to remove
	 */
	public void remove(
			final NormalizationWeightingFactor normalizationWeightingFactor) {
		normalizationWeightingFactors.remove(normalizationWeightingFactor);
		support.firePropertyChange("normalizationWeightingFactors",
				normalizationWeightingFactor, null);
	}

	/**
	 * Removes a property change listener
	 * 
	 * @param listener
	 *            The listener to remove
	 */
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * Setter of the reference system
	 * 
	 * @param referenceSystem
	 *            The reference system of the set
	 */
	public void setReferenceSystem(final String referenceSystem) {
		support.firePropertyChange("referenceSystem", this.referenceSystem,
				this.referenceSystem = referenceSystem);
	}

	/**
	 * Setter of the unit
	 * 
	 * @param unit
	 *            The weighting unit of the set
	 */
	public void setUnit(final String unit) {
		support.firePropertyChange("unit", this.unit, this.unit = unit);
	}

}
