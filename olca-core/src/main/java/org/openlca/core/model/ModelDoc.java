package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A common documentation class with meta-data fields for processes, product
 * systems, and EPDs.
 */
@Entity
@Table(name = "tbl_model_docs")
public class ModelDoc extends AbstractEntity implements Copyable<ModelDoc> {

	/**
	 * A semicolon separated list of synonyms.
	 */
	@Lob
	@Column(name = "synonyms")
	public String synonyms;

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


	@Override
	public ModelDoc copy() {
		var copy = new ModelDoc();
		copy.synonyms = synonyms;
		// TODO: copy fields
		return copy;
	}
}
