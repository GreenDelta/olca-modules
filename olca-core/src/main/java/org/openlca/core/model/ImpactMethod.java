package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_impact_methods")
public class ImpactMethod extends RootEntity {

	/**
	 * A code, short name, or abbreviation that identifies this impact assessment
	 * method (like 'EF 3.0' for environmental footprint 3.0).
	 */
	@Column(name = "code")
	public String code;

	@OneToMany
	@JoinTable(name = "tbl_impact_links",
			joinColumns = {@JoinColumn(name = "f_impact_method")},
			inverseJoinColumns = {@JoinColumn(name = "f_impact_category")})
	public final List<ImpactCategory> impactCategories = new ArrayList<>();

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	public final List<NwSet> nwSets = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_source")
	public Source source;

	public static ImpactMethod of(String name) {
		var method = new ImpactMethod();
		Entities.init(method, name);
		return method;
	}

	public ImpactMethod add(ImpactCategory impact) {
		if(impact == null)
			return this;
		impactCategories.add(impact);
		return this;
	}

	public ImpactMethod add(NwSet nwSet) {
		if (nwSet == null)
			return this;
		nwSets.add(nwSet);
		return this;
	}

	@Override
	public ImpactMethod copy() {
		var copy = new ImpactMethod();
		Entities.copyFields(this, copy);
		copy.code = code;
		copy.impactCategories.addAll(impactCategories);
		copy.source = source;
		for (var nwSet : nwSets) {
			copy.nwSets.add(nwSet.copy());
		}
		return copy;
	}

}
