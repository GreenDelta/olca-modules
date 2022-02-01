package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_impact_results")
public class ImpactResult extends AbstractEntity
	implements Copyable<ImpactResult> {

	@OneToOne
	@JoinColumn(name = "f_impact_category")
	public ImpactCategory indicator;

	@Column(name = "amount")
	public double amount;

	@Column(name = "description")
	public String description;

	public static ImpactResult of(ImpactCategory indicator, double amount) {
		var impact = new ImpactResult();
		impact.indicator = indicator;
		impact.amount = amount;
		return impact;
	}

	@Override
	public ImpactResult copy() {
		var clone = new ImpactResult();
		clone.indicator = indicator;
		clone.amount = amount;
		clone.description = description;
		return clone;
	}
}
