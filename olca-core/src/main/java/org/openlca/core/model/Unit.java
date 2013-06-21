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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * An unit of measurement (i.e. kg)
 * </p>
 */
@Entity
@Table(name = "tbl_units")
public class Unit extends AbstractEntity implements Cloneable {

	@Column(name = "conversion_factor")
	private double conversionFactor = 1d;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "name")
	private String name;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Column(name = "synonyms")
	private String synonyms;

	public Unit() {
	}

	public Unit(final String id, final String name) {
		setId(id);
		this.name = name;
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
	 * <p style="margin-top: 0">
	 * Getter of the conversionFactor-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The conversion factor to the reference unit in the parent unit
	 *         group of the unit
	 *         </p>
	 */
	public double getConversionFactor() {
		return conversionFactor;
	}

	@Override
	public Unit clone() {
		final Unit unit = new Unit(getId(), getName());
		unit.setConversionFactor(getConversionFactor());
		unit.setDescription(getDescription());
		unit.setSynonyms(getSynonyms());
		return unit;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the description-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The description of the unit
	 *         </p>
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the name field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The name of the unit
	 *         </p>
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the synonyms-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The synonyms of the unit, seperated by a semicolon
	 *         </p>
	 */
	public String getSynonyms() {
		return synonyms;
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
	 * Setter of the conversionFactor-field
	 * </p>
	 * 
	 * @param conversionFactor
	 *            <p style="margin-top: 0">
	 *            The conversion factor to the reference unit in the parent unit
	 *            group of the unit
	 *            </p>
	 */
	public void setConversionFactor(final double conversionFactor) {
		support.firePropertyChange("conversionFactor", this.conversionFactor,
				this.conversionFactor = conversionFactor);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the description-field
	 * </p>
	 * 
	 * @param description
	 *            <p style="margin-top: 0">
	 *            The description of the unit
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
	 *            The name of the unit
	 *            </p>
	 */
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the synonyms-field
	 * </p>
	 * 
	 * @param synonyms
	 *            <p style="margin-top: 0">
	 *            The synonyms of the unit, seperated by a semicolon
	 *            </p>
	 */
	public void setSynonyms(final String synonyms) {
		support.firePropertyChange("synonyms", this.synonyms,
				this.synonyms = synonyms);
	}

}
