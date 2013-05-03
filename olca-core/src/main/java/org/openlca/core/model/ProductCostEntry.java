package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_product_cost_entries")
public class ProductCostEntry extends AbstractEntity {

	@Column(name = "f_exchange")
	private String exchangeId;

	@Column(name = "f_process")
	private String processId;

	@OneToOne
	@JoinColumn(name = "f_cost_category")
	private CostCategory costCategory;

	@Column(name = "amount")
	private double amount;

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}

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

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProductCostEntry))
			return false;
		ProductCostEntry in = (ProductCostEntry) obj;
		if ((in.costCategory != null && in.costCategory.equals(costCategory))
				|| costCategory == null)
			if ((in.exchangeId != null && in.exchangeId.equals(exchangeId))
					|| exchangeId == null)
				if ((in.processId != null && in.processId.equals(processId))
						|| processId == null)
					return true;
		return false;
	}

}
