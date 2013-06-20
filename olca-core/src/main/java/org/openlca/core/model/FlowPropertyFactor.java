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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A conversion factor between two quantities of a flow.
 */
@Entity
@Table(name = "tbl_flowpropertyfactors")
public class FlowPropertyFactor extends AbstractEntity {

	@Column(name = "conversion_factor")
	private double conversionFactor = 1d;

	@OneToOne
	@JoinColumn(name = "f_flow_property")
	private FlowProperty flowProperty;

	@Override
	public FlowPropertyFactor clone() {
		final FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setConversionFactor(getConversionFactor());
		factor.setFlowProperty(getFlowProperty());
		factor.setId(UUID.randomUUID().toString());
		return factor;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public FlowProperty getFlowProperty() {
		return flowProperty;
	}

	public void setFlowProperty(FlowProperty flowProperty) {
		this.flowProperty = flowProperty;
	}

}
