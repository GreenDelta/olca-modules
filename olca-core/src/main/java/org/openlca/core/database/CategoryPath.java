package org.openlca.core.database;

import org.openlca.core.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryPath {

	private CategoryPath() {
	}

	/** Contains two categories at maximum: parent-category/category. */
	public static String getShort(String categoryId, IDatabase database) {
		if (categoryId == null || database == null)
			return "";
		Category category = getFromDb(categoryId, database);
		if (category == null || isRoot(category))
			return "";
		String path = "";
		Category parent = category.getParentCategory();
		if (isNotRoot(category))
			path = path.concat(parent.getName()).concat(" / ");
		return path.concat(category.getName());
	}

	private static Category getFromDb(String id, IDatabase db) {
		try {
			Category category = db.select(Category.class, id);
			return category;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(CategoryPath.class);
			log.error("failed to load category " + id, e);
			return null;
		}
	}

	private static boolean isRoot(Category category) {
		return !isNotRoot(category);
	}

	private static boolean isNotRoot(Category category) {
		return category != null && category.getParentCategory() != null
				&& category.getParentCategory().getParentCategory() != null;
	}

}
