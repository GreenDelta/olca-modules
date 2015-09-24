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
	public Exchange exchange;

	@OneToOne
	@JoinColumn(name = "f_cost_category")
	public CostCategory costCategory;

	@Column(name = "amount")
	public double amount;

	@Override
	public ProcessCostEntry clone() {
		ProcessCostEntry clone = new ProcessCostEntry();
		clone.exchange = exchange;
		clone.costCategory = costCategory;
		clone.amount = amount;
		return clone;
	}
}
