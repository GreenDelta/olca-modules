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
 * The technology objects hold information about the technology used in a
 * process
 * </p>
 */
@Entity
@Table(name = "tbl_technologies")
public class Technology extends AbstractEntity implements Cloneable {

	/**
	 * <p style="margin-top: 0">
	 * The description of the technology
	 * </p>
	 */
	@Lob
	@Column(name = "description")
	private String description;

	/**
	 * <p style="margin-top: 0">
	 * The property change support of the technology
	 * </p>
	 */
	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * <p style="margin-top: 0">
	 * Creates a new technology object
	 * </p>
	 */
	public Technology() {
	}

	/**
	 * <p style="margin-top: 0">
	 * Creates a new technology object for the given process
	 * 
	 * @param process
	 *            The owner process of the technology object
	 *            </p>
	 */
	public Technology(final Process process) {
		setId(process.getId());
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

	@Override
	public Technology clone() {
		final Technology technology = new Technology();
		technology.setDescription(getDescription());
		return technology;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the description-Field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The description of the technology
	 *         </p>
	 */
	public String getDescription() {
		return description;
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
	 * Setter of the description-Field
	 * </p>
	 * 
	 * @param description
	 *            <p style="margin-top: 0">
	 *            The description of the technology
	 *            </p>
	 */
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

}
