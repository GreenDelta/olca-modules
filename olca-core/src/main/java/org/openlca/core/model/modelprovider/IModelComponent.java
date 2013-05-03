/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model.modelprovider;

import java.beans.PropertyChangeListener;

/**
 * <p style="margin-top: 0">
 * Interface for openLCA model components
 * </p>
 */
public interface IModelComponent {

	/**
	 * <p style="margin-top: 0">
	 * Adds a property change listener to the support
	 * 
	 * @param listener
	 *            The property change listener to be added
	 *            </p>
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * <p style="margin-top: 0">
	 * Getter of the categoryId-field
	 * </p>
	 * 
	 * @return The id of the category of the model component
	 */
	String getCategoryId();

	/**
	 * <p style="margin-top: 0">
	 * Getter of the description-field
	 * </p>
	 * 
	 * @return The description of the model component
	 */
	String getDescription();

	/**
	 * <p style="margin-top: 0">
	 * Getter of the id-field
	 * </p>
	 * 
	 * @return The id of the model component
	 */
	String getId();

	/**
	 * <p style="margin-top: 0">
	 * Getter of the name-field
	 * </p>
	 * 
	 * @return The name of the model component
	 */
	String getName();

	/**
	 * <p style="margin-top: 0">
	 * Removes a property change listener from the support
	 * 
	 * @param listener
	 *            The property change listener to be removed
	 *            </p>
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

	/**
	 * <p style="margin-top: 0">
	 * Setter of the categoryKey-field
	 * </p>
	 * 
	 * @param categoryId
	 *            The id of the category of the model component
	 */
	void setCategoryId(String categoryId);

	/**
	 * <p style="margin-top: 0">
	 * Setter of the description-field
	 * </p>
	 * 
	 * @param description
	 *            The description of the model component
	 */
	void setDescription(String description);

	/**
	 * <p style="margin-top: 0">
	 * Setter of the id-field
	 * </p>
	 * 
	 * @param id
	 *            The id of the model component
	 */
	void setId(String id);

	/**
	 * <p style="margin-top: 0">
	 * Setter of the name-field
	 * </p>
	 * 
	 * @param name
	 *            The name of the model component
	 */
	void setName(String name);

}
