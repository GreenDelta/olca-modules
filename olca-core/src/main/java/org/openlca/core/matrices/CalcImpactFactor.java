package org.openlca.core.matrices;

import org.openlca.core.model.UncertaintyDistributionType;

class CalcImpactFactor {

	private long imactCategoryId;
	private long flowId;
	private double conversionFactor;
	private double amount;
	private UncertaintyDistributionType uncertaintyType;
	private double parameter1;
	private double parameter2;
	private double parameter3;

	public long getImactCategoryId() {
		return imactCategoryId;
	}

	public void setImactCategoryId(long imactCategoryId) {
		this.imactCategoryId = imactCategoryId;
	}

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public UncertaintyDistributionType getUncertaintyType() {
		return uncertaintyType;
	}

	public void setUncertaintyType(UncertaintyDistributionType uncertaintyType) {
		this.uncertaintyType = uncertaintyType;
	}

	public double getParameter1() {
		return parameter1;
	}

	public void setParameter1(double parameter1) {
		this.parameter1 = parameter1;
	}

	public double getParameter2() {
		return parameter2;
	}

	public void setParameter2(double parameter2) {
		this.parameter2 = parameter2;
	}

	public double getParameter3() {
		return parameter3;
	}

	public void setParameter3(double parameter3) {
		this.parameter3 = parameter3;
	}

}
