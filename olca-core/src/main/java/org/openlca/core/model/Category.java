package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.openlca.util.Categories;

@Entity
@Table(name = "tbl_categories")
public class Category extends RootEntity {

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "f_category")
	public final List<Category> childCategories = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "model_type")
	public ModelType modelType;

	public static Category of(String name, ModelType type) {
		var category = new Category();
		Entities.init(category, name);
		category.refId = Categories.createRefId(category);
		category.modelType = type;
		return category;
	}

	public static Category childOf(Category parent, String name) {
		var child = new Category();
		child.name = name;
		child.modelType = parent.modelType;
		parent.childCategories.add(child);
		child.category = parent;
		child.refId = Categories.createRefId(child);
		return child;
	}

	@Override
	public Category copy() {
		var clone = new Category();
		Entities.copyFields(this, clone);
		clone.modelType = modelType;
		for (var child : childCategories) {
			var childCopy = child.copy();
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

	public String toPath() {
		if (category == null)
			return name != null ? name.trim() : "";
		var prefix = category.toPath();
		return name != null
			? prefix + "/" + name.trim()
			: prefix;
	}

}
