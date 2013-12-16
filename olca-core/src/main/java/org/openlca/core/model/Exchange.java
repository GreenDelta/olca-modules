package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Exchanges are representing the inputs and outputs of flows in processes.
 */
@Entity
@Table(name = "tbl_exchanges")
public class Exchange extends AbstractEntity {

	@Column(name = "avoided_product")
	private boolean avoidedProduct;

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

	@OneToOne
	@JoinColumn(name = "f_unit")
	private Unit unit;

	@Column(name = "pedigree_uncertainty")
	private String pedigreeUncertainty;

	@Embedded
	private Uncertainty uncertainty;

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

	public Uncertainty getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(Uncertainty uncertainty) {
		this.uncertainty = uncertainty;
	}

	@Override
	public String toString() {
		return "Exchange [flow=" + flow + ", input=" + input + ",amount="
				+ amountValue + ", unit=" + unit + "]";
	}

	@Override
	public Exchange clone() {
		Exchange clone = new Exchange();
		clone.setAmountFormula(this.getAmountFormula());
		clone.setAmountValue(this.getAmountValue());
		clone.setAvoidedProduct(this.isAvoidedProduct());
		clone.setBaseUncertainty(this.getBaseUncertainty());
		clone.setDefaultProviderId(this.getDefaultProviderId());
		clone.setFlow(this.getFlow());
		clone.setFlowPropertyFactor(this.getFlowPropertyFactor());
		clone.setInput(this.isInput());
		clone.setPedigreeUncertainty(this.getPedigreeUncertainty());
		if (this.getUncertainty() != null)
			clone.setUncertainty(this.getUncertainty().clone());
		clone.setUnit(this.getUnit());
		return clone;
	}

}
