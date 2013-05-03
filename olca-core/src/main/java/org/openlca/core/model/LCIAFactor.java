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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * A factor for a specific flow that will be applied during calculation of the
 * LCIA result for a specific LCIA method
 * </p>
 */
@Entity
@Table(name = "tbl_lciafactors")
public class LCIAFactor extends AbstractEntity implements Copyable<LCIAFactor> {

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_flow")
	private Flow flow;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_flowpropertyfactor")
	private FlowPropertyFactor flowPropertyFactor;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@OneToOne(fetch = FetchType.EAGER)
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

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Applies the conversion factor of the unit and the flow property of the
	 * LCIA factor onto the resulting amount and returns the result
	 * </p>
	 * 
	 * @return The converted value
	 */
	public double getConvertedValue() {
		return getValue() / getFlowPropertyFactor().getConversionFactor()
				/ getUnit().getConversionFactor();
	}

	@Override
	public LCIAFactor copy() {
		final LCIAFactor lciaFactor = new LCIAFactor();
		lciaFactor.setId(UUID.randomUUID().toString());
		lciaFactor.setFlow(getFlow());
		lciaFactor.setFlowPropertyFactor(getFlowPropertyFactor());
		lciaFactor.setUnit(getUnit());
		lciaFactor.setValue(getValue());
		return lciaFactor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the flow-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The flow this factor belongs to
	 *         </p>
	 */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the flowPropertyFactor-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The flow property including the conversion factor
	 *         </p>
	 */
	public FlowPropertyFactor getFlowPropertyFactor() {
		return flowPropertyFactor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the unit-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The unit of the factor
	 *         </p>
	 */
	public Unit getUnit() {
		return unit;
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
	 * Setter of the flow-field
	 * </p>
	 * 
	 * @param flow
	 *            <p style="margin-top: 0">
	 *            The flow this factor belongs to
	 *            </p>
	 */
	public void setFlow(final Flow flow) {
		support.firePropertyChange("flow", this.flow, this.flow = flow);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the flowPropertyFactor-field
	 * </p>
	 * 
	 * @param flowPropertyFactor
	 *            <p style="margin-top: 0">
	 *            The flow property including the conversion factor
	 *            </p>
	 */
	public void setFlowPropertyFactor(
			final FlowPropertyFactor flowPropertyFactor) {
		support.firePropertyChange("flowPropertyFactor",
				this.flowPropertyFactor,
				this.flowPropertyFactor = flowPropertyFactor);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the unit-field
	 * </p>
	 * 
	 * @param unit
	 *            <p style="margin-top: 0">
	 *            The unit of the factor
	 *            </p>
	 */
	public void setUnit(final Unit unit) {
		support.firePropertyChange("unit", this.unit, this.unit = unit);
	}

	/**
	 * Setter of the value
	 * 
	 * @param value
	 *            The new value
	 */
	public void setValue(final double value) {
		support.firePropertyChange("value", this.value, this.value = value);
	}

	public UncertaintyDistributionType getUncertaintyType() {
		return uncertaintyType;
	}

	public void setUncertaintyType(UncertaintyDistributionType uncertaintyType) {
		support.firePropertyChange("uncertaintyType", this.uncertaintyType,
				this.uncertaintyType = uncertaintyType);
	}

	public double getUncertaintyParameter1() {
		return uncertaintyParameter1;
	}

	public void setUncertaintyParameter1(double uncertaintyParameter1) {
		support.firePropertyChange("uncertaintyParameter1",
				this.uncertaintyParameter1,
				this.uncertaintyParameter1 = uncertaintyParameter1);
	}

	public double getUncertaintyParameter2() {
		return uncertaintyParameter2;
	}

	public void setUncertaintyParameter2(double uncertaintyParameter2) {
		support.firePropertyChange("uncertaintyParameter2",
				this.uncertaintyParameter2,
				this.uncertaintyParameter2 = uncertaintyParameter2);
	}

	public double getUncertaintyParameter3() {
		return uncertaintyParameter3;
	}

	public void setUncertaintyParameter3(double uncertaintyParameter3) {
		support.firePropertyChange("uncertaintyParameter3",
				this.uncertaintyParameter3,
				this.uncertaintyParameter3 = uncertaintyParameter3);
	}

}
