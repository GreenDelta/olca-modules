package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * A scenario is basically just a set of parameter redefinitions with a name
 * and an optional description that is attached to a product system.
 */
@Entity
@Table(name = "tbl_scenarios")
public class Scenario extends AbstractEntity {

	@Column(name = "name")
	public String name;

	@Column(name = "description")
	public String description;

	@Column(name = "is_baseline")
	public boolean isBaseline;

	@JoinColumn(name = "f_owner")
	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	public final List<ParameterRedef> parameters = new ArrayList<>();

	@Override
	public Scenario clone() {
		Scenario clone = new Scenario();
		clone.name = name;
		clone.description = description;
		clone.isBaseline = isBaseline;
		for (ParameterRedef p : parameters) {
			if (p == null)
				continue;
			clone.parameters.add(p.clone());
		}
		return clone;
	}
}
