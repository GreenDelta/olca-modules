package org.openlca.core.database;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryDao extends BaseDao<Category> {

	public CategoryDao(EntityManagerFactory entityFactory) {
		super(Category.class, entityFactory);
	}

	/** Contains two categories at maximum: parent-category/category. */
	public String getShortPath(String categoryId) {
		if (categoryId == null)
			return "";
		try {
			Category category = getForId(categoryId);
			if (category == null || isRoot(category))
				return "";
			String path = "";
			Category parent = category.getParentCategory();
			if (isNotRoot(category))
				path = path.concat(parent.getName()).concat(" / ");
			return path.concat(category.getName());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to get category path for " + categoryId, e);
			return "";
		}
	}

	private boolean isRoot(Category category) {
		return !isNotRoot(category);
	}

	private boolean isNotRoot(Category category) {
		return category != null && category.getParentCategory() != null
				&& category.getParentCategory().getParentCategory() != null;
	}

}
