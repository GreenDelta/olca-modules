package org.openlca.core.results;

import org.openlca.core.model.CostCategory;

public class CostCategoryResult {

	private CostCategory costCategory;
	private double amount;

	public CostCategory getCostCategory() {
		return costCategory;
	}

	public void setCostCategory(CostCategory costCategory) {
		this.costCategory = costCategory;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

}
