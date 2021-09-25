package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_results")
public class ResultModel extends CategorizedEntity {

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_calculation_setup")
	public CalculationSetup setup;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_result")
	public final List<ResultFlow> inventory = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_result")
	public final List<ResultImpact> impacts = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_parent_result")
	public final List<ResultModel> subResults = new ArrayList<>();

	/**
	 * The timestamp when this result was calculated. A value of {@code <= 0}
	 * means that there is not such timestamp.
	 */
	@Column(name = "calculation_time")
	public long calculationTime;

	public static ResultModel of(String name) {
		var result = new ResultModel();
		Entities.init(result, name);
		return result;
	}

	@Override
	public ResultModel clone() {
		var clone = new ResultModel();
		Entities.copyRootFields(this, clone);
		if (setup != null) {
			clone.setup = setup.clone();
		}
		for (var flow : inventory) {
			clone.inventory.add(flow.clone());
		}
		for (var impact : impacts) {
			clone.impacts.add(impact.clone());
		}
		for (var subResult : subResults) {
			clone.subResults.add(subResult.clone());
		}
		clone.calculationTime = calculationTime;
		return clone;
	}
}
