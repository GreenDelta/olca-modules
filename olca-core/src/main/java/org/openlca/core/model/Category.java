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
import javax.persistence.Table;

@Entity
@Table(name = "tbl_categories")
public class Category extends CategorizedEntity {

	@OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true)
	@JoinColumn(name = "f_category")
	public final List<Category> childCategories = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "model_type")
	public ModelType modelType;

	public static Category of(String name, ModelType type) {
		var category = new Category();
		category.name = name;
		category.modelType = type;
		return category;
	}

	public static Category childOf(Category parent, String name) {
		var child = new Category();
		child.name = name;
		child.modelType = parent.modelType;
		parent.childCategories.add(child);
		child.category = parent;
		return child;
	}

	@Override
	public Category clone() {
		var clone = new Category();
		Util.copyFields(this, clone);
		clone.modelType = modelType;
		for (var child : childCategories) {
			var childCopy = child.clone();
			clone.childCategories.add(childCopy);
			childCopy.category = clone;
		}
		return clone;
	}

	@Override
	public String toString() {
		return String.format("Category {modelType=%s, refId=%s, name=%s}",
				modelType, refId, name);
	}

}
