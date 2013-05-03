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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.openlca.core.model.modelprovider.IModelComponent;

/**
 * <p style="margin-top: 0">
 * A project compares different product systems to each other
 * </p>
 */
@Entity
@Table(name = "tbl_projects")
public class Project extends AbstractEntity implements IModelComponent,
		Copyable<Project> {

	/**
	 * <p style="margin-top: 0">
	 * The author of the project
	 * </p>
	 */
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_author")
	private Actor author;

	/**
	 * <p style="margin-top: 0">
	 * The id of the category of the project
	 * </p>
	 */
	@Column(length = 36, name = "categoryid")
	private String categoryId;

	/**
	 * <p style="margin-top: 0">
	 * The creation date of the project
	 * </p>
	 */
	@Temporal(value = TemporalType.DATE)
	@Column(name = "creationdate")
	private Date creationDate;

	/**
	 * <p style="margin-top: 0">
	 * The description of the project
	 * </p>
	 */
	@Lob
	@Column(name = "description")
	private String description;

	/**
	 * <p style="margin-top: 0">
	 * The functional unit of the project
	 * </p>
	 */
	@Lob
	@Column(name = "functionalunit")
	private String functionalUnit;

	/**
	 * <p style="margin-top: 0">
	 * The goal of the project
	 * </p>
	 */
	@Lob
	@Column(name = "goal")
	private String goal;

	/**
	 * <p style="margin-top: 0">
	 * The date the project was last modified
	 * </p>
	 */
	@Temporal(value = TemporalType.DATE)
	@Column(name = "lastmodificationdate")
	private Date lastModificationDate;

	/**
	 * <p style="margin-top: 0">
	 * The name of the project
	 * </p>
	 */
	@Column(name = "name")
	private String name;

	/**
	 * Array of product system ids
	 */
	@Column(name = "productsystems")
	private String productSystemArray = "";

	/**
	 * <p style="margin-top: 0">
	 * The product systems to be compared in the project
	 * </p>
	 */
	@Transient
	private final List<String> productSystems = new ArrayList<>();

	/**
	 * <p style="margin-top: 0">
	 * The property change support of the project
	 * </p>
	 */
	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * <p style="margin-top: 0">
	 * Creates a new project
	 * </p>
	 */
	public Project() {
	}

	/**
	 * Creates a new project with the given name and id
	 * 
	 * @param id
	 *            The unique identifier of the project
	 * @param name
	 *            The name of the project
	 */
	public Project(final String id, final String name) {
		setId(id);
		this.name = name;
	}

	/**
	 * Maps the product systems of the project to string
	 */
	private void mapArray() {
		productSystemArray = "";
		for (int i = 0; i < productSystems.size(); i++) {
			productSystemArray += productSystems.get(i);
			if (i != productSystems.size() - 1) {
				productSystemArray += ";";
			}
		}
	}

	/**
	 * Initializes the property change listener after object is loaded from
	 * database
	 */
	@PostLoad
	protected void postLoad() {
		if (productSystemArray != null && productSystemArray.length() > 0) {
			for (final String id : productSystemArray.split(";")) {
				if (id.length() == 36) {
					productSystems.add(id);
				}
			}
		}
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a product system to the project
	 * </p>
	 * 
	 * @param id
	 *            The id of the product system to be added
	 */
	public void addProductSystem(final String id) {
		if (!productSystems.contains(id)) {
			productSystems.add(id);
			support.firePropertyChange("productSystems", null, id);
			mapArray();
		}
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * Looks up the project if it contains a specific product system
	 * 
	 * @param id
	 *            The id of the product system
	 * @return True if the project contains the product system with the
	 *         specified id, false otherwise
	 */
	public boolean containsProductSystem(final String id) {
		return productSystems.contains(id);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the author-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The author of the project
	 *         </p>
	 */
	public Actor getAuthor() {
		return author;
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public Project copy() {
		final Project project = new Project(UUID.randomUUID().toString(),
				getName());
		project.setAuthor(getAuthor());
		project.setCategoryId(getCategoryId());
		project.setCreationDate(getCreationDate());
		project.setDescription(getDescription());
		project.setFunctionalUnit(getFunctionalUnit());
		project.setGoal(getGoal());
		project.setLastModificationDate(getLastModificationDate());
		for (final String id : getProductSystems()) {
			project.addProductSystem(id);
		}
		return project;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the creationDate-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The creation date of the project
	 *         </p>
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the functionalUnit-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The functional unit of the project
	 *         </p>
	 */
	public String getFunctionalUnit() {
		return functionalUnit;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the goal-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The goal of the project
	 *         </p>
	 */
	public String getGoal() {
		return goal;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the lastModificationDate-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The date the project was last modified
	 *         </p>
	 */
	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the product system ids
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The product systems to be compared in the project
	 *         </p>
	 */
	public String[] getProductSystems() {
		return productSystems.toArray(new String[productSystems.size()]);
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a product system from the project
	 * </p>
	 * 
	 * @param id
	 *            The id product system to be removed
	 */
	public void removeProductSystem(final String id) {
		if (productSystems.contains(id)) {
			productSystems.remove(id);
			support.firePropertyChange("productSystems", id, null);
			mapArray();
		}
	}

	@Override
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the author-field
	 * </p>
	 * 
	 * @param author
	 *            <p style="margin-top: 0">
	 *            The author of the project
	 *            </p>
	 */
	public void setAuthor(final Actor author) {
		support.firePropertyChange("author", this.author, this.author = author);
	}

	@Override
	public void setCategoryId(final String categoryId) {
		support.firePropertyChange("categoryId", this.categoryId,
				this.categoryId = categoryId);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the creationDate-field
	 * </p>
	 * 
	 * @param creationDate
	 *            <p style="margin-top: 0">
	 *            The creation date of the project
	 *            </p>
	 */
	public void setCreationDate(final Date creationDate) {
		support.firePropertyChange("creationDate", this.creationDate,
				this.creationDate = creationDate);
	}

	@Override
	public void setDescription(final String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the functionalUnit-field
	 * </p>
	 * 
	 * @param functionalUnit
	 *            <p style="margin-top: 0">
	 *            The functional unit of the project
	 *            </p>
	 */
	public void setFunctionalUnit(final String functionalUnit) {
		support.firePropertyChange("functionalUnit", this.functionalUnit,
				this.functionalUnit = functionalUnit);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the goal-field
	 * </p>
	 * 
	 * @param goal
	 *            <p style="margin-top: 0">
	 *            The goal of the project
	 *            </p>
	 */
	public void setGoal(final String goal) {
		support.firePropertyChange("goal", this.goal, this.goal = goal);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the lastModificationDate-field
	 * </p>
	 * 
	 * @param lastModification
	 *            The last modification date
	 */
	public void setLastModificationDate(final Date lastModification) {
		support.firePropertyChange("lastModificationDate",
				this.lastModificationDate,
				this.lastModificationDate = lastModification);
	}

	@Override
	public void setName(final String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

}
