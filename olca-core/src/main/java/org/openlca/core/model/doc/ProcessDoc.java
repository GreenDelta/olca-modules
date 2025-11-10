package org.openlca.core.model.doc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.persistence.annotations.Mutable;
import org.openlca.commons.Copyable;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Source;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
			@JoinColumn(name = "f_owner")}, inverseJoinColumns = {
			@JoinColumn(name = "f_source")})
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

	@Lob
	@Mutable
	@Column(name = "flow_completeness")
	@Convert(converter = AspectMapConverter.class)
	public final AspectMap flowCompleteness = new AspectMap();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<ComplianceDeclaration> complianceDeclarations = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<Review> reviews = new ArrayList<>();

	// region goal & scope

	@Lob
	@Column(name = "intended_application")
	public String intendedApplication;

	@Lob
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

		if (validFrom != null) {
			clone.validFrom = new Date(validFrom.getTime());
		}
		if (validUntil != null) {
			clone.validUntil = new Date(validUntil.getTime());
		}
		clone.time = time;
		clone.geography = geography;
		clone.technology = technology;

		clone.inventoryMethod = inventoryMethod;
		clone.modelingConstants = modelingConstants;

		clone.dataCompleteness = dataCompleteness;
		clone.dataSelection = dataSelection;
		clone.dataTreatment = dataTreatment;
		clone.sources.addAll(sources);
		clone.samplingProcedure = samplingProcedure;
		clone.dataCollectionPeriod = dataCollectionPeriod;
		clone.useAdvice = useAdvice;

		clone.flowCompleteness.putAll(flowCompleteness);
		for (var c : complianceDeclarations) {
			clone.complianceDeclarations.add(c.copy());
		}
		for (var r : reviews) {
			clone.reviews.add(r.copy());
		}

		clone.intendedApplication = intendedApplication;
		clone.project = project;

		clone.dataGenerator = dataGenerator;
		clone.dataDocumentor = dataDocumentor;

		clone.creationDate = creationDate;
		clone.precedingDataSet = precedingDataSet;
		clone.publication = publication;
		clone.dataOwner = dataOwner;
		clone.copyright = copyright;
		clone.accessRestrictions = accessRestrictions;

		return clone;
	}

}
