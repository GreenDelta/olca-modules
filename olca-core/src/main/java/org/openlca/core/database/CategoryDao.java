package org.openlca.core.database;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;

public class CategoryDao extends BaseDao<Category> {

	public CategoryDao(EntityManagerFactory entityFactory) {
		super(Category.class, entityFactory);
	}

}
