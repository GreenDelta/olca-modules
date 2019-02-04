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
	public Category category;

}
