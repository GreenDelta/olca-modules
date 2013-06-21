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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A single impact assessment factor.
 */
@Entity
@Table(name = "tbl_lciafactors")
public class ImpactFactor extends AbstractEntity implements Cloneable {

	@OneToOne
	@JoinColumn(name = "f_flow")
	private Flow flow;

	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	private FlowPropertyFactor flowPropertyFactor;

	@OneToOne
	@JoinColumn(name = "f_unit")
	private Unit unit;

	@Column(name = "value")
	private double value = 1;

	@Column(name = "uncertainy_type")
	@Enumerated(EnumType.STRING)
	private UncertaintyDistributionType uncertaintyType;

	@Column(name = "uncertainty_parameter_1")
	private double uncertaintyParameter1;

	@Column(name = "uncertainty_parameter_2")
	private double uncertaintyParameter2;

	@Column(name = "uncertainty_parameter_3")
	private double uncertaintyParameter3;

	public double getConvertedValue() {
		return getValue() / getFlowPropertyFactor().getConversionFactor()
				/ getUnit().getConversionFactor();
	}

	@Override
	public ImpactFactor clone() {
		final ImpactFactor lciaFactor = new ImpactFactor();
		lciaFactor.setId(UUID.randomUUID().toString());
		lciaFactor.setFlow(getFlow());
		lciaFactor.setFlowPropertyFactor(getFlowPropertyFactor());
		lciaFactor.setUnit(getUnit());
		lciaFactor.setValue(getValue());
		return lciaFactor;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public FlowPropertyFactor getFlowPropertyFactor() {
		return flowPropertyFactor;
	}

	public void setFlowPropertyFactor(FlowPropertyFactor flowPropertyFactor) {
		this.flowPropertyFactor = flowPropertyFactor;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public UncertaintyDistributionType getUncertaintyType() {
		return uncertaintyType;
	}

	public void setUncertaintyType(UncertaintyDistributionType uncertaintyType) {
		this.uncertaintyType = uncertaintyType;
	}

	public double getUncertaintyParameter1() {
		return uncertaintyParameter1;
	}

	public void setUncertaintyParameter1(double uncertaintyParameter1) {
		this.uncertaintyParameter1 = uncertaintyParameter1;
	}

	public double getUncertaintyParameter2() {
		return uncertaintyParameter2;
	}

	public void setUncertaintyParameter2(double uncertaintyParameter2) {
		this.uncertaintyParameter2 = uncertaintyParameter2;
	}

	public double getUncertaintyParameter3() {
		return uncertaintyParameter3;
	}

	public void setUncertaintyParameter3(double uncertaintyParameter3) {
		this.uncertaintyParameter3 = uncertaintyParameter3;
	}

}
