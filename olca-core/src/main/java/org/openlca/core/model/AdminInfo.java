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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * The admin info holds the administrative information of a process
 * </p>
 */
@Entity
@Table(name = "tbl_admininfos")
public class AdminInfo extends AbstractEntity implements Cloneable {

	/**
	 * <p style="margin-top: 0">
	 * Access and use restrictions of the process
	 * </p>
	 */
	@Lob
	@Column(name = "accessanduserestrictions")
	private String accessAndUseRestrictions;

	/**
	 * <p style="margin-top: 0">
	 * Indicates if the data set is under copyright
	 * </p>
	 */
	@Column(name = "copyright")
	private boolean copyright;

	/**
	 * <p style="margin-top: 0">
	 * The creation date of the data set
	 * </p>
	 */
	@Temporal(value = TemporalType.DATE)
	@Column(name = "creationdate")
	private Date creationDate;

	/**
	 * <p style="margin-top: 0">
	 * The data documentor
	 * </p>
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_datadocumentor")
	private Actor dataDocumentor;

	/**
	 * <p style="margin-top: 0">
	 * The data generator
	 * </p>
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_datagenerator")
	private Actor dataGenerator;

	/**
	 * <p style="margin-top: 0">
	 * The data set owner
	 * </p>
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_datasetowner")
	private Actor dataSetOwner;

	/**
	 * The intended application of the process
	 */
	@Lob
	@Column(name = "intendedapplication")
	private String intendedApplication;

	/**
	 * <p style="margin-top: 0">
	 * The last change made on the data set
	 * </p>
	 */
	@Temporal(value = TemporalType.DATE)
	@Column(name = "lastchange")
	private Date lastChange;

	/**
	 * <p style="margin-top: 0">
	 * The project the process is in
	 * </p>
	 */
	@Column(name = "project")
	private String project;

	/**
	 * <p style="margin-top: 0">
	 * The source the data is publicated
	 * </p>
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_publication")
	private Source publication;

	/**
	 * <p style="margin-top: 0">
	 * The property change support of the adminInfo object
	 * </p>
	 */
	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * <p style="margin-top: 0">
	 * The version of the process
	 * </p>
	 */
	@Column(name = "version")
	private String version;

	/**
	 * <p style="margin-top: 0">
	 * Public constructor
	 * </p>
	 */
	public AdminInfo() {
	}

	/**
	 * <p style="margin-top: 0">
	 * Creates a new admin info object for the given process
	 * 
	 * @param process
	 *            The owner process of the new admin info
	 *            </p>
	 */
	public AdminInfo(final Process process) {
		setId(process.getId());
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

	/**
	 * <p style="margin-top: 0">
	 * Getter of the accessAndUseRestrictions-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Access and use restrictions of the process
	 *         </p>
	 */
	public String getAccessAndUseRestrictions() {
		return accessAndUseRestrictions;
	}

	@Override
	public AdminInfo clone() {
		final AdminInfo adminInfo = new AdminInfo();
		adminInfo.setAccessAndUseRestrictions(getAccessAndUseRestrictions());
		adminInfo.setCopyright(getCopyright());
		adminInfo.setCreationDate(getCreationDate());
		adminInfo.setDataDocumentor(getDataDocumentor());
		adminInfo.setDataGenerator(getDataGenerator());
		adminInfo.setDataSetOwner(getDataSetOwner());
		adminInfo.setIntendedApplication(getIntendedApplication());
		adminInfo.setLastChange(getLastChange());
		adminInfo.setProject(getProject());
		adminInfo.setPublication(getPublication());
		adminInfo.setVersion(getVersion());
		return adminInfo;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the copyright-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         Indicates if the data set is under copyright
	 *         </p>
	 */
	public boolean getCopyright() {
		return copyright;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the creationDate-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The creation date of the data set
	 *         </p>
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the dataDocumentor-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The data documentor
	 *         </p>
	 */
	public Actor getDataDocumentor() {
		return dataDocumentor;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the dataGenerator-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The data generator
	 *         </p>
	 */
	public Actor getDataGenerator() {
		return dataGenerator;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the dataSetOwner-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The data set owner
	 *         </p>
	 */
	public Actor getDataSetOwner() {
		return dataSetOwner;
	}

	/**
	 * Getter of the intendedApplication-field
	 * 
	 * @return The intended application of the process
	 */
	public String getIntendedApplication() {
		return intendedApplication;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the lastChange-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The last change made on the data set
	 *         </p>
	 */
	public Date getLastChange() {
		return lastChange;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the project-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The project the process is in
	 *         </p>
	 */
	public String getProject() {
		return project;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the publication-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The source the data is publicated
	 *         </p>
	 */
	public Source getPublication() {
		return publication;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the version-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The version of the process
	 *         </p>
	 */
	public String getVersion() {
		return version;
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
	 * Setter of the accessAndUseRestrictions-field
	 * </p>
	 * 
	 * @param accessAndUseRestrictions
	 *            <p style="margin-top: 0">
	 *            Access and use restrictions of the process
	 *            </p>
	 */
	public void setAccessAndUseRestrictions(
			final String accessAndUseRestrictions) {
		support.firePropertyChange("accessAndUseRestrictions",
				this.accessAndUseRestrictions,
				this.accessAndUseRestrictions = accessAndUseRestrictions);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the copyright-field
	 * </p>
	 * 
	 * @param copyright
	 *            <p style="margin-top: 0">
	 *            Indicates if the data set is under copyright
	 *            </p>
	 */
	public void setCopyright(final boolean copyright) {
		support.firePropertyChange("copyright", this.copyright,
				this.copyright = copyright);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the creationDate-field
	 * </p>
	 * 
	 * @param creationDate
	 *            <p style="margin-top: 0">
	 *            The creation date of the data set
	 *            </p>
	 */
	public void setCreationDate(final Date creationDate) {
		support.firePropertyChange("creationDate", this.creationDate,
				this.creationDate = creationDate);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the dataDocumentor-field
	 * </p>
	 * 
	 * @param dataDocumentor
	 *            <p style="margin-top: 0">
	 *            The data documentor
	 *            </p>
	 */
	public void setDataDocumentor(final Actor dataDocumentor) {
		support.firePropertyChange("dataDocumentor", this.dataDocumentor,
				this.dataDocumentor = dataDocumentor);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the dataGenerator-field
	 * </p>
	 * 
	 * @param dataGenerator
	 *            <p style="margin-top: 0">
	 *            The data generator
	 *            </p>
	 */
	public void setDataGenerator(final Actor dataGenerator) {
		support.firePropertyChange("dataGenerator", this.dataGenerator,
				this.dataGenerator = dataGenerator);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the dataSetOwner-field
	 * </p>
	 * 
	 * @param dataSetOwner
	 *            <p style="margin-top: 0">
	 *            The data set owner
	 *            </p>
	 */
	public void setDataSetOwner(final Actor dataSetOwner) {
		support.firePropertyChange("dataSetOwner", this.dataSetOwner,
				this.dataSetOwner = dataSetOwner);
	}

	/**
	 * Setter of the intendedApplication-field
	 * 
	 * @param intendedApplication
	 *            The intended application of the process
	 */
	public void setIntendedApplication(final String intendedApplication) {
		support.firePropertyChange("intendedApplication",
				this.intendedApplication,
				this.intendedApplication = intendedApplication);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the lastChange-field
	 * </p>
	 * 
	 * @param lastChange
	 *            <p style="margin-top: 0">
	 *            The last change made on the data set
	 *            </p>
	 */
	public void setLastChange(final Date lastChange) {
		support.firePropertyChange("lastChange", this.lastChange,
				this.lastChange = lastChange);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the project-field
	 * </p>
	 * 
	 * @param project
	 *            <p style="margin-top: 0">
	 *            The project the process is in
	 *            </p>
	 */
	public void setProject(final String project) {
		support.firePropertyChange("project", this.project,
				this.project = project);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the publication-field
	 * </p>
	 * 
	 * @param publication
	 *            <p style="margin-top: 0">
	 *            The source the data is publicated
	 *            </p>
	 */
	public void setPublication(final Source publication) {
		support.firePropertyChange("publication", this.publication,
				this.publication = publication);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the version-field
	 * </p>
	 * 
	 * @param version
	 *            <p style="margin-top: 0">
	 *            The version of the process
	 *            </p>
	 */
	public void setVersion(final String version) {
		support.firePropertyChange("version", this.version,
				this.version = version);
	}

}
