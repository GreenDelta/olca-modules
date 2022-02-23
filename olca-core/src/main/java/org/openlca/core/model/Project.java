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
@Table(name = "tbl_projects")
public class Project extends RootEntity {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	public final List<ProjectVariant> variants = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_impact_method")
	public ImpactMethod impactMethod;

	@OneToOne
	@JoinColumn(name = "f_nwset")
	public NwSet nwSet;

	@Column(name = "is_with_costs")
	public boolean isWithCosts;

	@Column(name = "is_with_regionalization")
	public boolean isWithRegionalization;

	public static Project of(String name) {
		var project = new Project();
		Entities.init(project, name);
		return project;
	}

	@Override
	public Project copy() {
		var clone = new Project();
		Entities.copyFields(this, clone);
		for (var variant : variants) {
			clone.variants.add(variant.copy());
		}
		clone.impactMethod = impactMethod;
		clone.nwSet = nwSet;
		clone.isWithCosts = isWithCosts;
		clone.isWithRegionalization = isWithRegionalization;
		return clone;
	}
}
