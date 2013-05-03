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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IModelComponent;

/**
 * <p style="margin-top: 0">
 * A source can be an instutiute or a book, which is used to know where the data
 * is coming from
 * </p>
 */
@Entity
@Table(name = "tbl_sources")
public class Source extends AbstractEntity implements Copyable<Source>, IModelComponent,
		IdentifyableByVersionAndUUID {

	@Column(length = 36, name = "categoryid")
	private String categoryId;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "doi")
	private String doi;

	@Column(name = "name")
	private String name;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Lob
	@Column(name = "textreference")
	private String textReference;

	@Column(name = "year")
	private Short year;

	public Source() {
	}

	public Source(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public Source copy() {
		final Source source = new Source(UUID.randomUUID().toString(),
				getName());
		source.setCategoryId(getCategoryId());
		source.setDescription(getDescription());
		source.setDoi(getDoi());
		source.setTextReference(getTextReference());
		source.setYear(getYear());
		return source;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the doi-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The doi of the source
	 *         </p>
	 */
	public String getDoi() {
		return doi;
	}

	@Override
	public String getUUID() {
		return getId();
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the textReference-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The text reference of the source
	 *         </p>
	 */
	public String getTextReference() {
		return textReference;
	}

	/**
	 * Getter of the year
	 * 
	 * @return The year of the source
	 */
	public Short getYear() {
		return year;
	}

	@Override
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	@Override
	public void setCategoryId(final String categoryId) {
		support.firePropertyChange("categoryId", this.categoryId,
				this.categoryId = categoryId);
	}

	@Override
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the doi-field
	 * </p>
	 * 
	 * @param doi
	 *            <p style="margin-top: 0">
	 *            The doi of the source
	 *            </p>
	 */
	public void setDoi(final String doi) {
		support.firePropertyChange("doi", this.doi, this.doi = doi);
	}

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the textReference-field
	 * </p>
	 * 
	 * @param textReference
	 *            <p style="margin-top: 0">
	 *            The text reference of the source
	 *            </p>
	 */
	public void setTextReference(final String textReference) {
		support.firePropertyChange("textReference", this.textReference,
				this.textReference = textReference);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the year-field
	 * </p>
	 * 
	 * @param year
	 *            <p style="margin-top: 0">
	 *            The year of the source
	 *            </p>
	 */
	public void setYear(final Short year) {
		support.firePropertyChange("year", this.year, this.year = year);
	}

}
