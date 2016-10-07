package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

	@Override
	// see update(category)
	public Category insert(Category category) {
		category.setRefId(Category.createRefId(category));
		Category existing = getForRefId(category.getRefId());
		if (existing != null) {
			mergeChildren(existing, category);
			return update(existing);
		}
		return super.insert(category);
	}

	@Override
	// categories should be identified by their path, therefore the refID will
	// be generated depending on the category path. This way, we can treat the
	// category model as a normal entity and still compare categories by path
	public Category update(Category category) {
		String refId = category.getRefId();
		String newRefId = Category.createRefId(category);
		Category forRefId = getForRefId(newRefId);
		if (Objects.equals(refId, newRefId) || forRefId == null) {
			category.setRefId(newRefId);
			category = super.update(category);
			for (Category child : category.getChildCategories())
				update(child);
			return category;
		}
		mergeChildren(forRefId, category);
		forRefId = super.update(forRefId);
		for (Category child : forRefId.getChildCategories())
			update(child);
		return forRefId;
	}

	private void mergeChildren(Category into, Category from) {
		for (Category child : from.getChildCategories()) {
			if (contains(into.getChildCategories(), child))
				continue;
			child.setCategory(into);
			into.getChildCategories().add(child);
		}
	}

	private boolean contains(List<Category> categories, Category category) {
		for (Category child : categories)
			if (Category.createRefId(child).equals(Category.createRefId(category)))
				return true;
		return false;
	}

}
