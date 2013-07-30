package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

public class CategoryDao extends RootEntityDao<Category> {

	public CategoryDao(IDatabase database) {
		super(Category.class, database);
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories(ModelType type) throws Exception {
		String jpql = "select c from Category c where c.parentCategory is null "
				+ "and c.modelType = :type";
		return getAll(jpql, Collections.singletonMap("type", type));
	}

}
