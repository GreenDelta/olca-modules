package org.openlca.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "tbl_projects")
public class Project extends CategorizedEntity {

	@OneToOne
	@JoinColumn(name = "f_author")
	public Actor author;

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	public Date creationDate;

	@Lob
	@Column(name = "functional_unit")
	public String functionalUnit;

	@Lob
	@Column(name = "goal")
	public String goal;

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "last_modification_date")
	public Date lastModificationDate;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	public final List<ProjectVariant> variants = new ArrayList<>();

	@Column(name = "f_impact_method")
	public Long impactMethodId;

	@Column(name = "f_nwset")
	public Long nwSetId;

	@Override
	public Project clone() {
		var clone = new Project();
		Util.copyFields(this, clone);
		clone.author = author;
		clone.creationDate = creationDate;
		clone.functionalUnit = functionalUnit;
		clone.goal = goal;
		clone.lastModificationDate = lastModificationDate;
		for (ProjectVariant variant : variants)
			clone.variants.add(variant.clone());
		clone.impactMethodId = impactMethodId;
		clone.nwSetId = nwSetId;
		return clone;
	}

}
