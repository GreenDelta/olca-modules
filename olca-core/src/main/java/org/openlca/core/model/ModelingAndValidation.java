/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * A modeling and validation object holds the modeling and validation
 * information of a process
 * </p>
 */
@Entity
@Table(name = "tbl_modelingandvalidations")
public class ModelingAndValidation extends AbstractEntity implements
		Copyable<ModelingAndValidation> {

	@Lob
	@Column(name = "datacollectionperiod")
	private String dataCollectionPeriod;

	@Lob
	@Column(name = "datacompleteness")
	private String dataCompleteness;

	@Lob
	@Column(name = "dataselection")
	private String dataSelection;

	@Lob
	@Column(name = "datasetotherevaluation")
	private String dataSetOtherEvaluation;

	@Lob
	@Column(name = "datatreatment")
	private String dataTreatment;

	@Lob
	@Column(name = "lcimethod")
	private String lciMethod;

	@Lob
	@Column(name = "modelingconstants")
	private String modelingConstants;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_reviewer")
	private Actor reviewer;

	@Lob
	@Column(name = "sampling")
	private String sampling;

	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinTable(name = "tbl_modelingandvalidation_source", joinColumns = { @JoinColumn(name = "f_modelingandvalidation") }, inverseJoinColumns = { @JoinColumn(name = "f_source") })
	private final List<Source> sources = new ArrayList<>();

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * <p style="margin-top: 0">
	 * Creates a new modeling and validation object
	 * </p>
	 */
	public ModelingAndValidation() {
	}

	/**
	 * <p style="margin-top: 0">
	 * Creates a new modeling and validtion object for the given process
	 * 
	 * @param process
	 *            The owner process of the modeling and validation
	 *            </p>
	 */
	public ModelingAndValidation(final Process process) {
		setId(process.getId());
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a source to the process
	 * 
	 * @param source
	 *            The source to be added
	 *            </p>
	 */
	public void add(final Source source) {
		if (!sources.contains(source)) {
			sources.add(source);
			support.firePropertyChange("sources", null, source);
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a property change listener to the support
	 * 
	 * @param listener
	 *            The property change listener to be added
	 *            </p>
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public ModelingAndValidation copy() {
		final ModelingAndValidation modelingAndValidation = new ModelingAndValidation();
		modelingAndValidation
				.setDataCollectionPeriod(getDataCollectionPeriod());
		modelingAndValidation.setDataCompleteness(getDataCompleteness());
		modelingAndValidation.setDataSelection(getDataSelection());
		modelingAndValidation
				.setDataSetOtherEvaluation(getDataSetOtherEvaluation());
		modelingAndValidation.setDataTreatment(getDataTreatment());
		modelingAndValidation.setLCIMethod(getLCIMethod());
		modelingAndValidation.setModelingConstants(getModelingConstants());
		modelingAndValidation.setReviewer(getReviewer());
		modelingAndValidation.setSampling(getSampling());
		for (final Source source : getSources()) {
			modelingAndValidation.add(source);
		}
		return modelingAndValidation;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the dataCollectionPeriod-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The period the process data was collected
	 *         </p>
	 */
	public String getDataCollectionPeriod() {
		return dataCollectionPeriod;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the dataCompleteness-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Information about the data completeness of the process
	 *         </p>
	 */
	public String getDataCompleteness() {
		return dataCompleteness;
	}

	/**
	 * Getter of the dataSelection-field
	 * 
	 * @return The data selection method of the process
	 */
	public String getDataSelection() {
		return dataSelection;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the dataSetOtherEvalution-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Other evalution applied on the data set
	 *         </p>
	 */
	public String getDataSetOtherEvaluation() {
		return dataSetOtherEvaluation;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the dataTreatment-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Information about the treatment of the process data
	 *         </p>
	 */
	public String getDataTreatment() {
		return dataTreatment;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the lciMethod-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The LCI method used for the process
	 *         </p>
	 */
	public String getLCIMethod() {
		return lciMethod;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the modelingConstants-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Additional modeling constants
	 *         </p>
	 */
	public String getModelingConstants() {
		return modelingConstants;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the reviewer-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The reviewer of the process data
	 *         </p>
	 */
	public Actor getReviewer() {
		return reviewer;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the sampling-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The sampling method
	 *         </p>
	 */
	public String getSampling() {
		return sampling;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the sources
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The sources of the process data
	 *         </p>
	 */
	public Source[] getSources() {
		return sources.toArray(new Source[sources.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a source from the process
	 * 
	 * @param source
	 *            The source to be removed
	 *            </p>
	 */
	public void remove(final Source source) {
		sources.remove(source);
		support.firePropertyChange("sources", source, null);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a property change listener from the support
	 * 
	 * @param listener
	 *            The property change listener to be removed
	 *            </p>
	 */
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the dataCollectionPeriod-field
	 * </p>
	 * 
	 * @param dataCollectionPeriod
	 *            <p style="margin-top: 0">
	 *            The period the process data was collected
	 *            </p>
	 */
	public void setDataCollectionPeriod(final String dataCollectionPeriod) {
		support.firePropertyChange("dataCollectionPeriod",
				this.dataCollectionPeriod,
				this.dataCollectionPeriod = dataCollectionPeriod);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the dataCompleteness-field
	 * </p>
	 * 
	 * @param dataCompleteness
	 *            <p style="margin-top: 0">
	 *            Information about the data completeness of the process
	 *            </p>
	 */
	public void setDataCompleteness(final String dataCompleteness) {
		support.firePropertyChange("dataCompleteness", this.dataCompleteness,
				this.dataCompleteness = dataCompleteness);
	}

	/**
	 * Setter of the dataSelection-field
	 * 
	 * @param dataSelection
	 *            The data selection method of the process
	 */
	public void setDataSelection(final String dataSelection) {
		support.firePropertyChange("dataSelection", this.dataSelection,
				this.dataSelection = dataSelection);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the dataSetOtherEvaluation-field
	 * </p>
	 * 
	 * @param dataSetOtherEvaluation
	 *            <p style="margin-top: 0">
	 *            Other evalution applied on the data set
	 *            </p>
	 */
	public void setDataSetOtherEvaluation(final String dataSetOtherEvaluation) {
		support.firePropertyChange("dataSetOtherEvaluation",
				this.dataSetOtherEvaluation,
				this.dataSetOtherEvaluation = dataSetOtherEvaluation);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the dataTreatment-field
	 * </p>
	 * 
	 * @param dataTreatment
	 *            <p style="margin-top: 0">
	 *            Information about the treatment of the process data
	 *            </p>
	 */
	public void setDataTreatment(final String dataTreatment) {
		support.firePropertyChange("dataTreatment", this.dataTreatment,
				this.dataTreatment = dataTreatment);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the lciMethod-field
	 * </p>
	 * 
	 * @param lciMethod
	 *            <p style="margin-top: 0">
	 *            The LCI method used for the process
	 *            </p>
	 */
	public void setLCIMethod(final String lciMethod) {
		support.firePropertyChange("lciMethod", this.lciMethod,
				this.lciMethod = lciMethod);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the modelingConstants-field
	 * </p>
	 * 
	 * @param modelingConstants
	 *            <p style="margin-top: 0">
	 *            Additional modeling constants
	 *            </p>
	 */
	public void setModelingConstants(final String modelingConstants) {
		support.firePropertyChange("modelingConstants", this.modelingConstants,
				this.modelingConstants = modelingConstants);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the reviewer-field
	 * </p>
	 * 
	 * @param reviewer
	 *            <p style="margin-top: 0">
	 *            The reviewer of the process data
	 *            </p>
	 */
	public void setReviewer(final Actor reviewer) {
		support.firePropertyChange("reviewer", this.reviewer,
				this.reviewer = reviewer);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the sampling-field
	 * </p>
	 * 
	 * @param sampling
	 *            <p style="margin-top: 0">
	 *            The sampling method
	 *            </p>
	 */
	public void setSampling(final String sampling) {
		support.firePropertyChange("sampling", this.sampling,
				this.sampling = sampling);
	}

}
