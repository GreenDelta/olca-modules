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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * A flow property factor holds a conversion factor for a specific flow and flow
 * property. The conversion factor is the factor to the reference flow property
 * of the flow this flow property factor is in
 * </p>
 */
@Entity
@Table(name = "tbl_flowpropertyfactors")
public class FlowPropertyFactor extends AbstractEntity implements
		Copyable<FlowPropertyFactor>
// does not support IdentifyableByVersionAndUUID, id is installation-dependent
{

	/**
	 * <p style="margin-top: 0">
	 * The conversion factor of the flow property to the reference flow property
	 * </p>
	 */
	@Column(name = "conversionfactor")
	private double conversionFactor = 1d;

	/**
	 * <p style="margin-top: 0">
	 * The flow property the factor is belonging to
	 * </p>
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_flowproperty")
	private FlowProperty flowProperty;

	/**
	 * <p style="margin-top: 0">
	 * The property change support of the flow property factor object
	 * </p>
	 */
	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * <p style="margin-top: 0">
	 * Creates a new flow property factor
	 * </p>
	 */
	public FlowPropertyFactor() {
	}

	/**
	 * <p style="margin-top: 0">
	 * Creates a new flow property factor with the given unique identifier, flow
	 * property and conversion factor
	 * </p>
	 * 
	 * @param id
	 *            <p style="margin-top: 0">
	 *            The unique identifier of the flow property factor
	 *            </p>
	 * @param flowProperty
	 *            <p style="margin-top: 0">
	 *            The flow property the factor is belonging to
	 *            </p>
	 * @param conversionFactor
	 *            <p style="margin-top: 0">
	 *            The conversion factor of the flow property to the reference
	 *            flow property
	 *            </p>
	 */
	public FlowPropertyFactor(final String id, final FlowProperty flowProperty,
			final double conversionFactor) {
		setId(id);
		this.flowProperty = flowProperty;
		this.conversionFactor = conversionFactor;
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
	 *         The conversion factor of the flow property to the reference flow
	 *         property
	 *         </p>
	 */
	public double getConversionFactor() {
		return conversionFactor;
	}

	@Override
	public FlowPropertyFactor copy() {
		final FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setConversionFactor(getConversionFactor());
		factor.setFlowProperty(getFlowProperty());
		factor.setId(UUID.randomUUID().toString());
		return factor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the flowProperty-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The flow property the factor is belonging to
	 *         </p>
	 */
	public FlowProperty getFlowProperty() {
		return flowProperty;
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
	 *            The conversion factor of the flow property to the reference
	 *            flow property
	 *            </p>
	 */
	public void setConversionFactor(final double conversionFactor) {
		support.firePropertyChange("conversionFactor", this.conversionFactor,
				this.conversionFactor = conversionFactor);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the flowProperty-field
	 * </p>
	 * 
	 * @param flowProperty
	 *            <p style="margin-top: 0">
	 *            The flow property the factor is belonging to
	 *            </p>
	 */
	public void setFlowProperty(final FlowProperty flowProperty) {
		support.firePropertyChange("flowProperty", this.flowProperty,
				this.flowProperty = flowProperty);
	}

}
