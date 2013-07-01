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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * Factor for the causal allocation method
 * </p>
 */
@Entity
@Table(name = "tbl_allocation_factors")
public class AllocationFactor extends AbstractEntity implements Cloneable {

	/**
	 * <p style="margin-top: 0">
	 * The id of the product exchange the factor is belonging to
	 * </p>
	 */
	@Column(length = 36, name = "productid")
	private String productId;

	/**
	 * <p style="margin-top: 0">
	 * The property change support of the allocation factor
	 * </p>
	 */
	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * <p style="margin-top: 0">
	 * The value of the factor
	 * </p>
	 */
	@Column(name = "value")
	private double value;

	/**
	 * <p style="margin-top: 0">
	 * Creates a new allocation factor
	 * </p>
	 */
	public AllocationFactor() {
	}

	/**
	 * Creates a new allocation factor with the given id, product id and value
	 * 
	 * @param id
	 *            The unique identifier of the allocation factor
	 * @param productId
	 *            The id of the product the allocation factor belongs to
	 * @param value
	 *            The factor itself
	 */
	public AllocationFactor(final String id, final String productId,
			final double value) {
		setId(id);
		this.productId = productId;
		this.value = value;
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
	public AllocationFactor clone() {
		return new AllocationFactor(UUID.randomUUID().toString(),
				getProductId(), getValue());
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the productKey-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The key of the product exchange the factor is belonging to
	 *         </p>
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the value-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The value of the factor
	 *         </p>
	 */
	public double getValue() {
		return value;
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
	 * Setter of the productId-field
	 * </p>
	 * 
	 * @param productId
	 *            <p style="margin-top: 0">
	 *            The id of the product exchange the factor is belonging to
	 *            </p>
	 */
	public void setProductId(final String productId) {
		support.firePropertyChange("productKey", this.productId,
				this.productId = productId);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the value-field
	 * </p>
	 * 
	 * @param value
	 *            <p style="margin-top: 0">
	 *            The value of the factor
	 *            </p>
	 */
	public void setValue(final double value) {
		support.firePropertyChange("value", this.value, this.value = value);
	}

}
