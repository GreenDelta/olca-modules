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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Exchanges are representing the inputs and outputs of flows in processes. For
 * the amounts of an exchange uncertainty distributions can be defined. Three
 * fields are reserved for the distribution parameters: <br>
 * <br>
 * 
 * parameter 1:
 * <ul>
 * <li>Normal distribution: arithmetic mean value
 * <li>Lognormal distribution: geometric mean value
 * <li>Triangle distribution: min value
 * <li>Uniform distribution: min value
 * </ul>
 * 
 * parameter 2:
 * <ul>
 * <li>Normal distribution: arithmetic standard deviation
 * <li>Lognormal distribution: geometric standard deviation
 * <li>Triangle distribution: most likely value
 * <li>Uniform distribution: max value
 * </ul>
 * 
 * parameter 3:
 * <ul>
 * <li>Triangle distribution: max value
 * </ul>
 */
@Entity
@Table(name = "tbl_exchanges")
public class Exchange extends AbstractEntity {

	@Column(name = "avoided_product")
	private boolean avoidedProduct;

	@Column(name = "distribution_type")
	private UncertaintyDistributionType distributionType = UncertaintyDistributionType.NONE;

	@OneToOne
	@JoinColumn(name = "f_flow")
	private Flow flow;

	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	private FlowPropertyFactor flowPropertyFactor;

	@Column(name = "is_input")
	private boolean input;

	@Column(name = "base_uncertainty")
	private Double baseUncertainty;

	@Column(name = "f_default_provider")
	private long defaultProviderId;

	@Column(name = "resulting_amount_value")
	private double amountValue;

	@Column(name = "resulting_amount_formula")
	private String amountFormula;

	@Column(name = "parameter1_value")
	private Double parameter1Value;

	@Column(name = "parameter1_formula")
	private String parameter1Formula;

	@Column(name = "parameter2_value")
	private Double parameter2Value;

	@Column(name = "parameter2_formula")
	private String parameter2Formula;

	@Column(name = "parameter3_value")
	private Double parameter3Value;

	@Column(name = "parameter3_formula")
	private String parameter3Formula;

	@OneToOne
	@JoinColumn(name = "f_unit")
	private Unit unit;

	@Column(name = "pedigree_uncertainty")
	private String pedigreeUncertainty;

	public UncertaintyDistributionType getDistributionType() {
		return distributionType;
	}

	public double getAmountValue() {
		return amountValue;
	}

	public void setAmountValue(double amountValue) {
		this.amountValue = amountValue;
	}

	public String getAmountFormula() {
		return amountFormula;
	}

	public void setAmountFormula(String amountFormula) {
		this.amountFormula = amountFormula;
	}

	public Double getParameter1Value() {
		return parameter1Value;
	}

	public void setParameter1Value(Double parameter1Value) {
		this.parameter1Value = parameter1Value;
	}

	public String getParameter1Formula() {
		return parameter1Formula;
	}

	public void setParameter1Formula(String parameter1Formula) {
		this.parameter1Formula = parameter1Formula;
	}

	public Double getParameter2Value() {
		return parameter2Value;
	}

	public void setParameter2Value(Double parameter2Value) {
		this.parameter2Value = parameter2Value;
	}

	public String getParameter2Formula() {
		return parameter2Formula;
	}

	public void setParameter2Formula(String parameter2Formula) {
		this.parameter2Formula = parameter2Formula;
	}

	public Double getParameter3Value() {
		return parameter3Value;
	}

	public void setParameter3Value(Double parameter3Value) {
		this.parameter3Value = parameter3Value;
	}

	public String getParameter3Formula() {
		return parameter3Formula;
	}

	public void setParameter3Formula(String parameter3Formula) {
		this.parameter3Formula = parameter3Formula;
	}

	public void setDistributionType(UncertaintyDistributionType distributionType) {
		this.distributionType = distributionType;
	}

	public Flow getFlow() {
		return flow;
	}

	public FlowPropertyFactor getFlowPropertyFactor() {
		return flowPropertyFactor;
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

	public void setAvoidedProduct(boolean avoidedProduct) {
		this.avoidedProduct = avoidedProduct;
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
		return "Exchange [flow=" + flow + ", input=" + input + ",amount="
				+ amountValue + ", unit=" + unit + "]";
	}

}
