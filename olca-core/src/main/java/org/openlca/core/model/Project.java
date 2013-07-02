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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * A project compares different product systems to each other
 * </p>
 */
@Entity
@Table(name = "tbl_projects")
public class Project extends CategorizedEntity {

	@OneToOne
	@JoinColumn(name = "f_author")
	private Actor author;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "creation_date")
	private Date creationDate;

	@Lob
	@Column(name = "functional_unit")
	private String functionalUnit;

	@Lob
	@Column(name = "goal")
	private String goal;

	@Temporal(value = TemporalType.DATE)
	@Column(name = "last_modification_date")
	private Date lastModificationDate;

	@Column(name = "product_systems")
	private String productSystemArray = "";

	@Transient
	private final List<String> productSystems = new ArrayList<>();

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

	public void addProductSystem(final String id) {
		if (!productSystems.contains(id)) {
			productSystems.add(id);
			mapArray();
		}
	}

	public boolean containsProductSystem(final String id) {
		return productSystems.contains(id);
	}

	public Actor getAuthor() {
		return author;
	}

	@Override
	public Project clone() {
		final Project project = new Project();
		project.setId(UUID.randomUUID().toString());
		project.setName(getName());
		project.setAuthor(getAuthor());
		project.setCategory(getCategory());
		project.setCreationDate(getCreationDate());
		project.setDescription(getDescription());
		project.setFunctionalUnit(getFunctionalUnit());
		project.setGoal(getGoal());
		project.setLastModificationDate(getLastModificationDate());
		for (String id : getProductSystems()) {
			project.addProductSystem(id);
		}
		return project;
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

	public String[] getProductSystems() {
		return productSystems.toArray(new String[productSystems.size()]);
	}

	public void removeProductSystem(final String id) {
		if (productSystems.contains(id)) {
			productSystems.remove(id);
			mapArray();
		}
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

}
