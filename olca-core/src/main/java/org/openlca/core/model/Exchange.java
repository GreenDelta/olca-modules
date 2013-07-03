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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * An exchange is a carrier of a flow (an elementary flows like CO2 or a product
 * flow). Exchanges are representing the inputs and outputs of processes.
 */
@Entity
@Table(name = "tbl_exchanges")
public class Exchange extends AbstractEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_exchange")
	private final List<AllocationFactor> allocationFactors = new ArrayList<>();

	@Column(name = "avoidedproduct")
	private boolean avoidedProduct;

	@Column(name = "distributionType")
	private UncertaintyDistributionType distributionType = UncertaintyDistributionType.NONE;

	@OneToOne
	@JoinColumn(name = "f_flow")
	private Flow flow;

	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	private FlowPropertyFactor flowPropertyFactor;

	@Column(name = "is_input")
	private boolean input;

	@Column(name = "parametrized")
	private boolean parametrized;

	@Column(name = "base_uncertainty")
	private Double baseUncertainty;

	@Column(name = "f_default_provider")
	private long defaultProviderId;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "resultingamount_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "resultingamount_formula")) })
	private final Expression resultingAmount = new Expression("1", 1d);

	/**
	 * Normal distribution: arithmetic mean value <br>
	 * Lognormal distribution: geometric mean value <br>
	 * Triangle distribution: min value <br>
	 * Uniform distribution: min value
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "parameter1_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "parameter1_formula")) })
	private Expression uncertaintyParameter1;

	/**
	 * Normal distribution: arithmetic standard deviation<br>
	 * Lognormal distribution: geometric standard deviation<br>
	 * Triangle distribution: most likely value<br>
	 * Uniform distribution: max value
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "parameter2_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "parameter2_formula")) })
	private Expression uncertaintyParameter2;

	/**
	 * Triangle distribution: max value
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "parameter3_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "parameter3_formula")) })
	private Expression uncertaintyParameter3;

	@OneToOne
	@JoinColumn(name = "f_unit")
	private Unit unit;

	@Column(name = "pedigree_uncertainty")
	private String pedigreeUncertainty;

	private void calculateResultingAmount(UncertaintyDistributionType type) {
		String formula = null;
		switch (type) {
		case NORMAL:
			formula = uncertaintyParameter1.getFormula();
			break;
		case LOG_NORMAL:
			formula = uncertaintyParameter1.getFormula();
			break;
		case TRIANGLE:
			formula = "(" + uncertaintyParameter1.getFormula() + " + "
					+ uncertaintyParameter2.getFormula() + " + "
					+ uncertaintyParameter3.getFormula() + ") / 3";
			break;
		case UNIFORM:
			formula = "(" + uncertaintyParameter1.getFormula() + " + "
					+ uncertaintyParameter2.getFormula() + ") / 2";
			break;
		default:
			break;
		}
		if (formula != null) {
			resultingAmount.setFormula(formula);
		}
	}

	public void add(AllocationFactor allocationFactor) {
		if (!allocationFactors.contains(allocationFactor)) {
			allocationFactors.add(allocationFactor);
		}
	}

	public AllocationFactor getAllocationFactor(long productId) {
		for (AllocationFactor factor : getAllocationFactors())
			if (factor.getProductId() == productId)
				return factor;
		return null;
	}

	public AllocationFactor[] getAllocationFactors() {
		return allocationFactors.toArray(new AllocationFactor[allocationFactors
				.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Applies the conversion factor of the unit and the flow property of the
	 * exchange onto the resulting amount and returns the result
	 * </p>
	 * 
	 * @return The converted result
	 */
	public double getConvertedResult() {
		return getResultingAmount().getValue()
				/ getFlowPropertyFactor().getConversionFactor()
				* getUnit().getConversionFactor();
	}

	public UncertaintyDistributionType getDistributionType() {
		return distributionType;
	}

	public Flow getFlow() {
		return flow;
	}

	public FlowPropertyFactor getFlowPropertyFactor() {
		return flowPropertyFactor;
	}

	public Expression getResultingAmount() {
		return resultingAmount;
	}

	/**
	 * Getter of the first uncertainty parameter
	 * 
	 * @return In case of: <br>
	 *         Normal distribution: arithmetic mean value<br>
	 *         Lognormal distribution: geometric mean value<br>
	 *         Triangle distribution: min value<br>
	 *         Uniform distribution: min value<br>
	 *         null otherwise
	 */
	public Expression getUncertaintyParameter1() {
		return uncertaintyParameter1;
	}

	/**
	 * Getter of the second uncertainty parameter
	 * 
	 * @return In case of: <br>
	 *         Normal distribution: arithmetic standard deviation<br>
	 *         Lognormal distribution: geometric standard deviation<br>
	 *         Triangle distribution: most likely value<br>
	 *         Uniform distribution: max value<br>
	 *         null otherwise
	 */
	public Expression getUncertaintyParameter2() {
		return uncertaintyParameter2;
	}

	/**
	 * Getter of the third uncertainty parameter
	 * 
	 * @return In case of: <br>
	 *         Triangle distribution: max value<br>
	 *         null otherwise
	 */
	public Expression getUncertaintyParameter3() {
		return uncertaintyParameter3;
	}

	public Unit getUnit() {
		return unit;
	}

	public boolean isAvoidedProduct() {
		return avoidedProduct;
	}

	public boolean isInput() {
		return input;
	}

	public boolean isParametrized() {
		return parametrized;
	}

	public void remove(AllocationFactor allocationFactor) {
		allocationFactors.remove(allocationFactor);
	}

	public void setAvoidedProduct(boolean avoidedProduct) {
		this.avoidedProduct = avoidedProduct;
	}

	public void setDistributionType(UncertaintyDistributionType distributionType) {
		if (this.distributionType == distributionType)
			return;
		resetUncertainty();
		if (distributionType != null
				&& distributionType != UncertaintyDistributionType.NONE) {
			setUncertaintyValues(distributionType);
			calculateResultingAmount(distributionType);
		}
		this.distributionType = distributionType;
	}

	private void resetUncertainty() {
		if (uncertaintyParameter1 != null)
			uncertaintyParameter1 = null;
		if (uncertaintyParameter2 != null)
			uncertaintyParameter2 = null;
		if (uncertaintyParameter3 != null)
			uncertaintyParameter3 = null;
	}

	private void setUncertaintyValues(
			UncertaintyDistributionType distributionType) {
		String formula = resultingAmount.getFormula();
		double value = resultingAmount.getValue();
		switch (distributionType) {
		case NORMAL:
			setUncertainty(1, formula, value);
			setUncertainty(2, "1", 1d);
			break;
		case LOG_NORMAL:
			setUncertainty(1, formula, value);
			setUncertainty(2, "1", 1d);
			break;
		case TRIANGLE:
			setUncertainty(1, formula, value);
			setUncertainty(2, formula, value);
			setUncertainty(3, formula, value);
			break;
		case UNIFORM:
			setUncertainty(1, formula, value);
			setUncertainty(2, formula, value);
			break;
		default:
			break;
		}
	}

	private void setUncertainty(int param, String formula, double value) {
		Expression e = new Expression(formula, value);
		switch (param) {
		case 1:
			uncertaintyParameter1 = e;
			break;
		case 2:
			uncertaintyParameter2 = e;
			break;
		case 3:
			uncertaintyParameter3 = e;
			break;
		default:
			break;
		}
	}

	public void setFlow(final Flow flow) {
		this.flow = flow;
	}

	public void setFlowPropertyFactor(FlowPropertyFactor flowPropertyFactor) {
		this.flowPropertyFactor = flowPropertyFactor;
	}

	public void setInput(boolean input) {
		this.input = input;
	}

	public void setUnit(final Unit unit) {
		this.unit = unit;
	}

	public String getPedigreeUncertainty() {
		return pedigreeUncertainty;
	}

	public void setPedigreeUncertainty(String pedigreeUncertainty) {
		this.pedigreeUncertainty = pedigreeUncertainty;
	}

	public Double getBaseUncertainty() {
		return baseUncertainty;
	}

	public void setBaseUncertainty(Double baseUncertainty) {
		this.baseUncertainty = baseUncertainty;
	}

	public long getDefaultProviderId() {
		return defaultProviderId;
	}

	public void setDefaultProviderId(long defaultProviderId) {
		this.defaultProviderId = defaultProviderId;
	}

	@Override
	public String toString() {
		return "Exchange [flow=" + flow + ", input=" + input
				+ ",resultingAmount=" + resultingAmount + ", unit=" + unit
				+ "]";
	}

}
