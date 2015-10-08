package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategoryDescriptor;

public class CategoryDao extends
		CategorizedEntityDao<Category, CategoryDescriptor> {

	public CategoryDao(IDatabase database) {
		super(Category.class, CategoryDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "version",
				"last_change", "f_category", "model_type" };
	}

	@Override
	protected CategoryDescriptor createDescriptor(Object[] queryResult) {
		CategoryDescriptor descriptor = super.createDescriptor(queryResult);
		if (queryResult[7] instanceof String)
			descriptor.setCategoryType(ModelType
					.valueOf((String) queryResult[7]));
		return descriptor;
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories(ModelType type) {
		String jpql = "select c from Category c where c.category is null "
				+ "and c.modelType = :type";
		return getAll(jpql, Collections.singletonMap("type", type));
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories() {
		String jpql = "select c from Category c where c.category is null";
		Map<String, Object> m = Collections.emptyMap();
		return getAll(jpql, m);
	}

}
