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
@Table(name = "tbl_projects")
public class Project extends CategorizedEntity {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	public final List<ProjectVariant> variants = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_impact_method")
	public ImpactMethod impactMethod;

	@OneToOne
	@JoinColumn(name = "f_nwset")
	public NwSet nwSet;

	public static Project of(String name) {
		var project = new Project();
		Entities.init(project, name);
		return project;
	}

	@Override
	public Project clone() {
		var clone = new Project();
		Entities.copyFields(this, clone);
		for (var variant : variants) {
			clone.variants.add(variant.clone());
		}
		clone.impactMethod = impactMethod;
		clone.nwSet = nwSet;
		return clone;
	}
}
