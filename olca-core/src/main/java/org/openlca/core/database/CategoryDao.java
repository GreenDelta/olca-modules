package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class CategoryDao extends RootEntityDao<Category, BaseDescriptor> {

	public CategoryDao(IDatabase database) {
		super(Category.class, BaseDescriptor.class, database);
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories(ModelType type) {
		String jpql = "select c from Category c where c.parentCategory is null "
				+ "and c.modelType = :type";
		return getAll(jpql, Collections.singletonMap("type", type));
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories() {
		String jpql = "select c from Category c where c.parentCategory is null";
		Map<String, Object> m = Collections.emptyMap();
		return getAll(jpql, m);
	}

}
