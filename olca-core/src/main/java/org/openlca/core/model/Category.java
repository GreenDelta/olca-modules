package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_categories")
public class Category extends RootEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_parent_category")
	private List<Category> childCategories = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "model_type")
	private ModelType modelType;

	@OneToOne
	@JoinColumn(name = "f_parent_category")
	private Category parentCategory;

	public ModelType getModelType() {
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	public List<Category> getChildCategories() {
		return childCategories;
	}

	@Override
	public Category clone() {
		Category clone = new Category();
		clone.setDescription(getDescription());
		clone.setModelType(getModelType());
		clone.setName(getName());
		clone.setParentCategory(getParentCategory());
		clone.setRefId(UUID.randomUUID().toString());
		for (Category child : getChildCategories()) {
			Category childCopy = child.clone();
			clone.getChildCategories().add(childCopy);
			childCopy.setParentCategory(clone);
		}
		return clone;
	}

	public Category getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(Category parentCategory) {
		this.parentCategory = parentCategory;
	}

	@Override
	public String toString() {
		return String.format("Category {modelType=%s, refId=%s, name=%s}",
				getModelType(), getRefId(), getName());
	}

}
