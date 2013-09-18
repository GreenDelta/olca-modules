package org.openlca.core.matrix;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;

public class CalcExchange {

	private long processId;
	private long flowId;
	private long exchangeId;
	private boolean input;
	private double conversionFactor;
	private double amount;
	private String amountFormula;
	private UncertaintyType uncertaintyType;
	private double parameter1;
	private double parameter2;
	private double parameter3;
	private String parameter1Formula;
	private String parameter2Formula;
	private String parameter3Formula;
	private FlowType flowType;
	private long defaultProviderId;

	public long getFlowId() {
		return flowId;
	}

	public void setFlowId(long flowId) {
		this.flowId = flowId;
	}

	public long getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(long exchangeId) {
		this.exchangeId = exchangeId;
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

	public String getAmountFormula() {
		return amountFormula;
	}

	public void setAmountFormula(String amountFormula) {
		this.amountFormula = amountFormula;
	}

	public UncertaintyType getUncertaintyType() {
		return uncertaintyType;
	}

	public void setUncertaintyType(UncertaintyType uncertaintyType) {
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

	public String getParameter1Formula() {
		return parameter1Formula;
	}

	public void setParameter1Formula(String parameter1Formula) {
		this.parameter1Formula = parameter1Formula;
	}

	public String getParameter2Formula() {
		return parameter2Formula;
	}

	public void setParameter2Formula(String parameter2Formula) {
		this.parameter2Formula = parameter2Formula;
	}

	public String getParameter3Formula() {
		return parameter3Formula;
	}

	public void setParameter3Formula(String parameter3Formula) {
		this.parameter3Formula = parameter3Formula;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public long getProcessId() {
		return processId;
	}

	public FlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(FlowType flowType) {
		this.flowType = flowType;
	}

	public boolean isInput() {
		return input;
	}

	public void setInput(boolean input) {
		this.input = input;
	}

	/** Returns 0 if the exchange has no default provider. */
	public long getDefaultProviderId() {
		return defaultProviderId;
	}

	public void setDefaultProviderId(long defaultProviderId) {
		this.defaultProviderId = defaultProviderId;
	}

}
