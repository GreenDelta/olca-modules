package org.openlca.io;

import org.openlca.core.model.Category;
import org.openlca.util.Strings;

public class CategoryPath {

	private CategoryPath() {
	}

	/**
	 * Returns the full category path from the root category to this category,
	 * or an empty string if the given category is null.
	 */
	public static String getFull(Category category) {
		if (category == null)
			return "";
		String path = category.name;
		Category parent = category.category;
		while (parent != null) {
			path = parent.name + "/" + path;
			parent = parent.category;
		}
		return path;
	}

	/**
	 * Max. 2 category names and 75 characters
	 */
	public static String getShort(Category category) {
		if (category == null)
			return "";
		if (category.category == null)
			return category.name;
		String shortPath = category.category.name + "/"
				+ category.name;
		return Strings.cut(shortPath, 75);
	}

}
