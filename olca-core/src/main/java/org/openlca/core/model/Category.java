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
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents a category of a model component
 * 
 * @author Sebastian Greve
 * 
 */
@Entity
@Table(name = "tbl_categories")
public class Category extends AbstractEntity implements Copyable<Category> {

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "f_parentcategory")
	private List<Category> childCategories = new ArrayList<>();

	@Column(name = "componentclass")
	private String componentClass;

	@Column(name = "name")
	private String name;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "f_parentcategory")
	private Category parentCategory;

	@Transient
	private transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public Category() {
	}

	/**
	 * Creates a new category with the given key, name and component class
	 * 
	 * @param id
	 *            The unique identifier of the category
	 * @param name
	 *            The name of the category
	 * @param componentClass
	 *            The class of the component that can use the category
	 */
	public Category(String id, String name, String componentClass) {
		setId(id);
		this.name = name;
		this.componentClass = componentClass;
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

	public String getComponentClass() {
		return componentClass;
	}

	@Override
	public Category copy() {
		Category category = new Category(UUID.randomUUID().toString(),
				getName(), getComponentClass());
		for (Category child : getChildCategories()) {
			Category childCopy = child.copy();
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

	/**
	 * Get the full path from the categories root to this category. Returns the
	 * category names separated by '/'.
	 */
	public String getFullPath() {
		String text = "";
		if (!getId().equals(getComponentClass())) {
			Category parentCategory = getParentCategory();
			while (parentCategory != null) {
				if (!parentCategory.getId().equals(
						parentCategory.getComponentClass())) {
					text = parentCategory.getName() + "/" + text;
					parentCategory = parentCategory.getParentCategory();
				} else {
					parentCategory = null;
				}
			}
			text += getName();
		}
		return text;
	}

	public void remove(Category childCategory) {
		childCategories.remove(childCategory);
		support.firePropertyChange("childCategories", childCategory, null);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void setComponentClass(String componentClass) {
		support.firePropertyChange("componentClass", this.componentClass,
				this.componentClass = componentClass);
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
		return String.format("Category {componentClass=%s, id=%s, name=%s}",
				componentClass, getId(), name);
	}

}
