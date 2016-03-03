package org.openlca.core.model;

import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

/**
 * A categorized entity is a root entity with a category.
 */
@MappedSuperclass
public abstract class CategorizedEntity extends RootEntity {

	@OneToOne
	@JoinColumn(name = "f_category")
	private Category category;

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

}
