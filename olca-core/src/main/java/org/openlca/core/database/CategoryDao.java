package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryDao extends BaseDao<Category> {

	public CategoryDao(EntityManagerFactory entityFactory) {
		super(Category.class, entityFactory);
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories(ModelType type) throws Exception {
		String jpql = "select c from Category c where c.parentCategory is null "
				+ "and c.modelType = :type";
		return getAll(jpql, Collections.singletonMap("type", type));
	}

}
