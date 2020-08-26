package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public Category clone() {
		Category clone = new Category();
		Util.copyRootFields(this, clone);
		clone.modelType = modelType;
		clone.category = category;
		for (Category child : childCategories) {
			Category childCopy = child.clone();
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
