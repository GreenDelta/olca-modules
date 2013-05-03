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
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * An LCIA category holds a set of LCIA factors which define for specific flows
 * how many they contribute to a specific LCIA method
 * </p>
 */
@Entity
@Table(name = "tbl_lciacategories")
public class LCIACategory extends AbstractEntity implements
		Copyable<LCIACategory>, PropertyChangeListener {

	@Lob
	@Column(name = "description")
	private String description;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_lciacategory")
	private final List<LCIAFactor> lciaFactors = new ArrayList<>();

	@Column(name = "name")
	private String name;

	@Column(name = "referenceunit")
	private String referenceUnit;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public LCIACategory() {
	}

	/**
	 * Initializes the property change listener after object is loaded from
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		for (final LCIAFactor factor : getLCIAFactors()) {
			factor.addPropertyChangeListener(this);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds an LCIA factor to the LCIA category
	 * 
	 * @param lciaFactor
	 *            The LCIA factor to be added
	 *            </p>
	 */
	public void add(final LCIAFactor lciaFactor) {
		if (!lciaFactors.contains(lciaFactor)) {
			lciaFactors.add(lciaFactor);
			support.firePropertyChange("lciaFactors", null, lciaFactor);
			lciaFactor.addPropertyChangeListener(this);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a property change listener to the support
	 * 
	 * @param listener
	 *            The property change listener to be added
	 *            </p>
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * Returns the converted LCIA factor value for the given flow
	 * 
	 * @param flow
	 *            The flow the LCIA factor is requested for
	 * @return The converted LCIA factor value for the given flow
	 */
	public double getConvertedLCIAFactor(final Flow flow) {
		LCIAFactor factor = null;
		int i = 0;
		while (factor == null && i < lciaFactors.size()) {
			final LCIAFactor actual = lciaFactors.get(i);
			if (actual.getFlow().getId().equals(flow.getId())) {
				factor = actual;
			} else {
				i++;
			}
		}
		return factor == null ? 0 : factor.getConvertedValue();
	}

	@Override
	public LCIACategory copy() {
		final LCIACategory lciaCategory = new LCIACategory();
		lciaCategory.setId(UUID.randomUUID().toString());
		lciaCategory.setDescription(getDescription());
		lciaCategory.setName(getName());
		lciaCategory.setReferenceUnit(getReferenceUnit());
		for (final LCIAFactor lciaFactor : getLCIAFactors()) {
			lciaCategory.add(lciaFactor.copy());
		}
		return lciaCategory;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the description-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         A description of the LCIA category
	 *         </p>
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the LCIA factors
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The LCIA factors of the LCIA category
	 *         </p>
	 */
	public LCIAFactor[] getLCIAFactors() {
		return lciaFactors.toArray(new LCIAFactor[lciaFactors.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the name-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The name of the LCIA category
	 *         </p>
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the referenceUnit-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The reference unit of the LCIA category
	 *         </p>
	 */
	public String getReferenceUnit() {
		return referenceUnit;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes an LCIA factor from the LCIA category
	 * 
	 * @param lciaFactor
	 *            The LCIA factor to be removed
	 *            </p>
	 */
	public void remove(final LCIAFactor lciaFactor) {
		lciaFactor.removePropertyChangeListener(this);
		lciaFactors.remove(lciaFactor);
		support.firePropertyChange("lciaFactors", lciaFactor, null);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a property change listener from the support
	 * 
	 * @param listener
	 *            The property change listener to be removed
	 *            </p>
	 */
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the description-field
	 * </p>
	 * 
	 * @param description
	 *            <p style="margin-top: 0">
	 *            A description of the LCIA category
	 *            </p>
	 */
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the name-field
	 * </p>
	 * 
	 * @param name
	 *            <p style="margin-top: 0">
	 *            The name of the LCIA category
	 *            </p>
	 */
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the referenceUnit-field
	 * </p>
	 * 
	 * @param referenceUnit
	 *            <p style="margin-top: 0">
	 *            The reference unit of the LCIA category
	 *            </p>
	 */
	public void setReferenceUnit(final String referenceUnit) {
		support.firePropertyChange("referenceUnit", this.referenceUnit,
				this.referenceUnit = referenceUnit);
	}

}
