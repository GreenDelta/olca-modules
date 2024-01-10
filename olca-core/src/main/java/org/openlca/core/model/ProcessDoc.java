package org.openlca.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

/**
 * Contains the general documentation fields of a process that are not used for
 * indexing, calculation, etc.
 */
@Entity
@Table(name = "tbl_process_docs")
public class ProcessDoc extends AbstractEntity implements Copyable<ProcessDoc> {

	// region time, geography, technology

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_from")
	public Date validFrom;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "valid_until")
	public Date validUntil;

	@Lob
	@Column(name = "time")
	public String time;

	@Lob
	@Column(name = "geography")
	public String geography;

	@Lob
	@Column(name = "technology")
	public String technology;

	// endregion

	// region inventory method

	@Lob
	@Column(name = "inventory_method")
	public String inventoryMethod;

	@Lob
	@Column(name = "modeling_constants")
	public String modelingConstants;

	// endregion

	// region data treatment, sources and representativeness

	@Lob
	@Column(name = "data_completeness")
	public String dataCompleteness;

	@Lob
	@Column(name = "data_selection")
	public String dataSelection;

	@Lob
	@Column(name = "data_treatment")
	public String dataTreatment;

	@OneToMany
	@JoinTable(name = "tbl_source_links", joinColumns = {
			@JoinColumn(name = "f_owner") }, inverseJoinColumns = {
			@JoinColumn(name = "f_source") })
	public final List<Source> sources = new ArrayList<>();

	@Lob
	@Column(name = "sampling_procedure")
	public String samplingProcedure;

	@Lob
	@Column(name = "data_collection_period")
	public String dataCollectionPeriod;

	@Lob
	@Column(name = "use_advice")
	public String useAdvice;

	// endregion

	// region review

	@Column(name="review_type")
	public String reviewType;

	@Lob
	@Column(name = "review_details")
	public String reviewDetails;

	@OneToOne
	@JoinColumn(name = "f_reviewer")
	public Actor reviewer;

	@OneToOne
	@JoinColumn(name = "f_review_report")
	public Source reviewReport;

  // endregion

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<ComplianceDeclaration> complianceDeclarations = new ArrayList<>();

	// region goal & scope

	@Lob
	@Column(name = "intended_application")
	public String intendedApplication;

	@Column(name = "project")
	public String project;

	// endregion

	@OneToOne
	@JoinColumn(name = "f_data_generator")
	public Actor dataGenerator;

	@OneToOne
	@JoinColumn(name = "f_data_documentor")
	public Actor dataDocumentor;


	// region publication & ownership

	@Temporal(value = TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	public Date creationDate;

	/**
	 * Contains an identifier of the preceding process data set from which this
	 * process was derived. In case of a data set imported from an EcoSpold 2
	 * file this could be the original activity ID for example (which is not
	 * unique between different data sets in ecoinvent) for example. Also, there
	 * is a corresponding field in the ILCD data exchange format.
	 */
	@Column(name = "preceding_dataset")
	public String precedingDataSet;

	@OneToOne
	@JoinColumn(name = "f_publication")
	public Source publication;

	@OneToOne
	@JoinColumn(name = "f_data_owner")
	public Actor dataOwner;

	@Column(name = "copyright")
	public boolean copyright;

	@Lob
	@Column(name = "access_restrictions")
	public String accessRestrictions;

	// endregion

	@Override
	public ProcessDoc copy() {
		var clone = new ProcessDoc();

		clone.technology = technology;
		clone.time = time;
		clone.validFrom = validFrom;
		clone.validUntil = validUntil;

		clone.useAdvice = useAdvice;

		for (var c : complianceDeclarations) {
			clone.complianceDeclarations.add(c.copy());
		}

		clone.dataCollectionPeriod = dataCollectionPeriod;
		clone.dataCompleteness = dataCompleteness;
		clone.dataSelection = dataSelection;
		clone.reviewDetails = reviewDetails;
		clone.dataTreatment = dataTreatment;
		clone.inventoryMethod = inventoryMethod;
		clone.modelingConstants = modelingConstants;
		clone.reviewer = reviewer;
		clone.samplingProcedure = samplingProcedure;
		clone.sources.addAll(sources);

		clone.accessRestrictions = accessRestrictions;
		clone.copyright = copyright;
		clone.creationDate = creationDate;
		clone.dataDocumentor = dataDocumentor;
		clone.dataGenerator = dataGenerator;
		clone.dataOwner = dataOwner;
		clone.intendedApplication = intendedApplication;
		clone.project = project;
		clone.publication = publication;
		clone.geography = geography;
		clone.precedingDataSet = precedingDataSet;

		return clone;
	}

}
