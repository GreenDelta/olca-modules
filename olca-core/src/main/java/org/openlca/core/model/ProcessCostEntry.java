package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A cost entry for a process product.
 */
@Entity
@Table(name = "tbl_process_cost_entries")
public class ProcessCostEntry extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_exchange")
	private Exchange exchange;

	@OneToOne
	@JoinColumn(name = "f_cost_category")
	private CostCategory costCategory;

	@Column(name = "amount")
	private double amount;

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
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

}
