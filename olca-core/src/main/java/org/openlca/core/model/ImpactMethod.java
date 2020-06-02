package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_impact_methods")
public class ImpactMethod extends CategorizedEntity {

	@OneToMany
	@JoinTable(name = "tbl_impact_links", joinColumns = {
			@JoinColumn(name = "f_impact_method") }, inverseJoinColumns = {
					@JoinColumn(name = "f_impact_category") })
	public final List<ImpactCategory> impactCategories = new ArrayList<>();

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_impact_method")
	public final List<NwSet> nwSets = new ArrayList<>();

	/**
	 * The original author of the method.
	 */
	@OneToOne
	@JoinColumn(name = "f_author")
	public Actor author;

	/**
	 * The person/organization that adapted/converted the method into this
	 * machine readable format.
	 */
	@OneToOne
	@JoinColumn(name = "f_generator")
	public Actor generator;

	@OneToMany
	@JoinTable(name = "tbl_source_links", joinColumns = {
			@JoinColumn(name = "f_owner") }, inverseJoinColumns = {
					@JoinColumn(name = "f_source") })
	public final List<Source> sources = new ArrayList<>();

	@Override
	public ImpactMethod clone() {
		ImpactMethod clone = new ImpactMethod();
		Util.cloneRootFields(this, clone);
		clone.category = category;
		for (ImpactCategory i : impactCategories) {
			clone.impactCategories.add(i);
		}

		clone.author = author;
		clone.generator = generator;
		for (Source source : sources) {
			clone.sources.add(source);
		}
		for (NwSet nwSet : nwSets) {
			clone.nwSets.add(nwSet.clone());
		}
		return clone;
	}

}
