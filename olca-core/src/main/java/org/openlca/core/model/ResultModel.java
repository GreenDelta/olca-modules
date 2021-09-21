package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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

	@Override
	public CategorizedEntity clone() {
		var clone = new ResultModel();
		Entities.copyRootFields(this, clone);

		if (setup != null) {
			clone.setup = setup.clone();
		}

		for (var flow : inventory) {
			if (flow != null) {
				clone.inventory.add(flow.clone());
			}
		}

		for (var impact : impacts) {
			if (impact != null) {
				clone.impacts.add(impact.clone());
			}
		}

		return clone;
	}
}
