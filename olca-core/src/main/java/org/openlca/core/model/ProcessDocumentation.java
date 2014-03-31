package org.openlca.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Contains the general documentation fields of a process that are not used for
 * indexing, calculation, etc.
 */
@Entity
@Table(name = "tbl_process_docs")
public class ProcessDocumentation extends AbstractEntity implements Cloneable {

	@Lob
	@Column(name = "time")
	private String time;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_until")
	private Date validUntil;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_from")
	private Date validFrom;

	@Lob
	@Column(name = "technology")
	private String technology;

	@Lob
	@Column(name = "data_collection_period")
	private String dataCollectionPeriod;

	@Lob
	@Column(name = "completeness")
	private String completeness;

	@Lob
	@Column(name = "data_selection")
	private String dataSelection;

	@Lob
	@Column(name = "review_details")
	private String reviewDetails;

	@Lob
	@Column(name = "data_treatment")
	private String dataTreatment;

	@Lob
	@Column(name = "inventory_method")
	private String inventoryMethod;

	@Lob
	@Column(name = "modeling_constants")
	private String modelingConstants;

	@OneToOne
	@JoinColumn(name = "f_reviewer")
	private Actor reviewer;

	@Lob
	@Column(name = "sampling")
	private String sampling;

	@OneToMany
	@JoinTable(name = "tbl_process_sources", joinColumns = { @JoinColumn(name = "f_process_doc") }, inverseJoinColumns = { @JoinColumn(name = "f_source") })
	private final List<Source> sources = new ArrayList<>();

	@Lob
	@Column(name = "restrictions")
	private String restrictions;

	@Column(name = "copyright")
	private boolean copyright;

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	private Date creationDate;

	@OneToOne
	@JoinColumn(name = "f_data_documentor")
	private Actor dataDocumentor;

	@OneToOne
	@JoinColumn(name = "f_data_generator")
	private Actor dataGenerator;

	@OneToOne
	@JoinColumn(name = "f_dataset_owner")
	private Actor dataSetOwner;

	@Lob
	@Column(name = "intended_application")
	private String intendedApplication;

	@Column(name = "project")
	private String project;

	@OneToOne
	@JoinColumn(name = "f_publication")
	private Source publication;

	@Lob
	@Column(name = "geography")
	private String geography;

	@Override
	public ProcessDocumentation clone() {
		ProcessDocumentation clone = new ProcessDocumentation();

		clone.setTechnology(getTechnology());
		clone.setTime(getTime());
		clone.setValidFrom(getValidFrom());
		clone.setValidUntil(getValidUntil());

		clone.setDataCollectionPeriod(getDataCollectionPeriod());
		clone.setCompleteness(getCompleteness());
		clone.setDataSelection(getDataSelection());
		clone.setReviewDetails(getReviewDetails());
		clone.setDataTreatment(getDataTreatment());
		clone.setInventoryMethod(getInventoryMethod());
		clone.setModelingConstants(getModelingConstants());
		clone.setReviewer(getReviewer());
		clone.setSampling(getSampling());
		for (Source source : getSources()) {
			clone.getSources().add(source);
		}

		clone.setRestrictions(getRestrictions());
		clone.setCopyright(isCopyright());
		clone.setCreationDate(getCreationDate());
		clone.setDataDocumentor(getDataDocumentor());
		clone.setDataGenerator(getDataGenerator());
		clone.setDataSetOwner(getDataSetOwner());
		clone.setIntendedApplication(getIntendedApplication());
		clone.setProject(getProject());
		clone.setPublication(getPublication());

		clone.setGeography(getGeography());

		return clone;
	}

	public String getDataCollectionPeriod() {
		return dataCollectionPeriod;
	}

	public void setDataCollectionPeriod(String dataCollectionPeriod) {
		this.dataCollectionPeriod = dataCollectionPeriod;
	}

	public String getCompleteness() {
		return completeness;
	}

	public void setCompleteness(String completeness) {
		this.completeness = completeness;
	}

	public String getDataSelection() {
		return dataSelection;
	}

	public void setDataSelection(String dataSelection) {
		this.dataSelection = dataSelection;
	}

	public String getReviewDetails() {
		return reviewDetails;
	}

	public void setReviewDetails(String reviewDetails) {
		this.reviewDetails = reviewDetails;
	}

	public String getDataTreatment() {
		return dataTreatment;
	}

	public void setDataTreatment(String dataTreatment) {
		this.dataTreatment = dataTreatment;
	}

	public String getInventoryMethod() {
		return inventoryMethod;
	}

	public void setInventoryMethod(String inventoryMethod) {
		this.inventoryMethod = inventoryMethod;
	}

	public String getModelingConstants() {
		return modelingConstants;
	}

	public void setModelingConstants(String modelingConstants) {
		this.modelingConstants = modelingConstants;
	}

	public Actor getReviewer() {
		return reviewer;
	}

	public void setReviewer(Actor reviewer) {
		this.reviewer = reviewer;
	}

	public String getSampling() {
		return sampling;
	}

	public void setSampling(String sampling) {
		this.sampling = sampling;
	}

	public List<Source> getSources() {
		return sources;
	}

	public String getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(String restrictions) {
		this.restrictions = restrictions;
	}

	public boolean isCopyright() {
		return copyright;
	}

	public void setCopyright(boolean copyright) {
		this.copyright = copyright;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Actor getDataDocumentor() {
		return dataDocumentor;
	}

	public void setDataDocumentor(Actor dataDocumentor) {
		this.dataDocumentor = dataDocumentor;
	}

	public Actor getDataGenerator() {
		return dataGenerator;
	}

	public void setDataGenerator(Actor dataGenerator) {
		this.dataGenerator = dataGenerator;
	}

	public Actor getDataSetOwner() {
		return dataSetOwner;
	}

	public void setDataSetOwner(Actor dataSetOwner) {
		this.dataSetOwner = dataSetOwner;
	}

	public String getIntendedApplication() {
		return intendedApplication;
	}

	public void setIntendedApplication(String intendedApplication) {
		this.intendedApplication = intendedApplication;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public Source getPublication() {
		return publication;
	}

	public void setPublication(Source publication) {
		this.publication = publication;
	}

	public String getGeography() {
		return geography;
	}

	public void setGeography(String geography) {
		this.geography = geography;
	}

	public String getTechnology() {
		return technology;
	}

	public void setTechnology(String technology) {
		this.technology = technology;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

}
