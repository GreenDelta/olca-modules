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
	private Actor author;

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	private Date creationDate;

	@Lob
	@Column(name = "functional_unit")
	private String functionalUnit;

	@Lob
	@Column(name = "goal")
	private String goal;

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "last_modification_date")
	private Date lastModificationDate;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	private final List<ProjectVariant> variants = new ArrayList<>();

	@Column(name = "f_impact_method")
	private Long impactMethodId;

	@Column(name = "f_nwset")
	private Long nwSetId;

	@Override
	public Project clone() {
		Project clone = new Project();
		Util.cloneRootFields(this, clone);
		clone.setAuthor(getAuthor());
		clone.setCategory(getCategory());
		clone.setCreationDate(getCreationDate());
		clone.setFunctionalUnit(getFunctionalUnit());
		clone.setGoal(getGoal());
		clone.setLastModificationDate(getLastModificationDate());
		for (ProjectVariant variant : getVariants())
			clone.getVariants().add(variant.clone());
		clone.setImpactMethodId(getImpactMethodId());
		clone.setNwSetId(getNwSetId());
		return clone;
	}

	public Actor getAuthor() {
		return author;
	}

	public List<ProjectVariant> getVariants() {
		return variants;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getFunctionalUnit() {
		return functionalUnit;
	}

	public String getGoal() {
		return goal;
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setAuthor(Actor author) {
		this.author = author;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setFunctionalUnit(String functionalUnit) {
		this.functionalUnit = functionalUnit;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public void setImpactMethodId(Long impactMethodId) {
		this.impactMethodId = impactMethodId;
	}

	public Long getImpactMethodId() {
		return impactMethodId;
	}

	public void setNwSetId(Long nwSetId) {
		this.nwSetId = nwSetId;
	}

	public Long getNwSetId() {
		return nwSetId;
	}

}
