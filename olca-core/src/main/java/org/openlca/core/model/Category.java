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
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tbl_categories")
public class Category extends AbstractEntity implements Cloneable {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_parent_category")
	private List<Category> childCategories = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "model_type")
	private ModelType modelType;

	@Column(name = "name")
	private String name;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_parent_category")
	private Category parentCategory;

	@Transient
	private transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public Category() {
	}

	public Category(String id, String name, ModelType modelType) {
		setId(id);
		this.name = name;
		this.modelType = modelType;
	}

	public ModelType getModelType() {
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	/**
	 * Adds a child category to the category
	 */
	public void add(Category childCategory) {
		if (!childCategories.contains(childCategory)) {
			childCategories.add(childCategory);
			support.firePropertyChange("childCategories", null, childCategory);
		}
	}

	/**
	 * Adds a property change listener to the support
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public Category[] getChildCategories() {
		return childCategories.toArray(new Category[childCategories.size()]);
	}

	@Override
	public Category clone() {
		Category category = new Category(UUID.randomUUID().toString(),
				getName(), getModelType());
		for (Category child : getChildCategories()) {
			Category childCopy = child.clone();
			category.add(childCopy);
			childCopy.setParentCategory(category);
		}
		category.setParentCategory(getParentCategory());
		return category;
	}

	public String getName() {
		return name;
	}

	public Category getParentCategory() {
		return parentCategory;
	}

	public void remove(Category childCategory) {
		childCategories.remove(childCategory);
		support.firePropertyChange("childCategories", childCategory, null);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void setName(String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	public void setParentCategory(Category parentCategory) {
		support.firePropertyChange("parentCategory", this.parentCategory,
				this.parentCategory = parentCategory);
	}

	@Override
	public String toString() {
		return String.format("Category {modelType=%s, id=%s, name=%s}",
				getModelType(), getId(), name);
	}

}
