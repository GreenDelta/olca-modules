package org.openlca.core.matrices;

public class CalcCostEntry {

	private long processId;
	private long exchangeId;
	private long costCategoryId;
	private double amount;

	public long getProcessId() {
		return processId;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

	public long getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(long exchangeId) {
		this.exchangeId = exchangeId;
	}

	public long getCostCategoryId() {
		return costCategoryId;
	}

	public void setCostCategoryId(long costCategoryId) {
		this.costCategoryId = costCategoryId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

}
