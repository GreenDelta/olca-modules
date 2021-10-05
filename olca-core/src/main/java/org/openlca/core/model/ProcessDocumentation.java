package org.openlca.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Contains the general documentation fields of a process that are not used for
 * indexing, calculation, etc.
 */
@Entity
@Table(name = "tbl_process_docs")
public class ProcessDocumentation extends AbstractEntity
	implements Copyable<ProcessDocumentation> {

	@Lob
	@Column(name = "time")
	public String time;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_until")
	public Date validUntil;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_from")
	public Date validFrom;

	@Lob
	@Column(name = "technology")
	public String technology;

	@Lob
	@Column(name = "data_collection_period")
	public String dataCollectionPeriod;

	@Lob
	@Column(name = "completeness")
	public String completeness;

	@Lob
	@Column(name = "data_selection")
	public String dataSelection;

	@Lob
	@Column(name = "review_details")
	public String reviewDetails;

	@Lob
	@Column(name = "data_treatment")
	public String dataTreatment;

	@Lob
	@Column(name = "inventory_method")
	public String inventoryMethod;

	@Lob
	@Column(name = "modeling_constants")
	public String modelingConstants;

	@OneToOne
	@JoinColumn(name = "f_reviewer")
	public Actor reviewer;

	@Lob
	@Column(name = "sampling")
	public String sampling;

	@OneToMany
	@JoinTable(name = "tbl_source_links", joinColumns = {
			@JoinColumn(name = "f_owner") }, inverseJoinColumns = {
					@JoinColumn(name = "f_source") })
	public final List<Source> sources = new ArrayList<>();

	@Lob
	@Column(name = "restrictions")
	public String restrictions;

	@Column(name = "copyright")
	public boolean copyright;

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	public Date creationDate;

	@OneToOne
	@JoinColumn(name = "f_data_documentor")
	public Actor dataDocumentor;

	@OneToOne
	@JoinColumn(name = "f_data_generator")
	public Actor dataGenerator;

	@OneToOne
	@JoinColumn(name = "f_dataset_owner")
	public Actor dataSetOwner;

	@Lob
	@Column(name = "intended_application")
	public String intendedApplication;

	@Column(name = "project")
	public String project;

	@OneToOne
	@JoinColumn(name = "f_publication")
	public Source publication;

	@Lob
	@Column(name = "geography")
	public String geography;

	/**
	 * Contains an identifier of the preceding process data set from which this
	 * process was derived. In case of a data set imported from an EcoSpold 2
	 * file this could be the original activity ID for example (which is not
	 * unique between different data sets in ecoinvent) for example. Also, there
	 * is a corresponding field in the ILCD data exchange format.
	 */
	@Column(name = "preceding_dataset")
	public String precedingDataSet;

	@Override
	public ProcessDocumentation copy() {
		var clone = new ProcessDocumentation();

		clone.technology = technology;
		clone.time = time;
		clone.validFrom = validFrom;
		clone.validUntil = validUntil;

		clone.dataCollectionPeriod = dataCollectionPeriod;
		clone.completeness = completeness;
		clone.dataSelection = dataSelection;
		clone.reviewDetails = reviewDetails;
		clone.dataTreatment = dataTreatment;
		clone.inventoryMethod = inventoryMethod;
		clone.modelingConstants = modelingConstants;
		clone.reviewer = reviewer;
		clone.sampling = sampling;
		clone.sources.addAll(sources);

		clone.restrictions = restrictions;
		clone.copyright = copyright;
		clone.creationDate = creationDate;
		clone.dataDocumentor = dataDocumentor;
		clone.dataGenerator = dataGenerator;
		clone.dataSetOwner = dataSetOwner;
		clone.intendedApplication = intendedApplication;
		clone.project = project;
		clone.publication = publication;
		clone.geography = geography;
		clone.precedingDataSet = precedingDataSet;

		return clone;
	}

}
