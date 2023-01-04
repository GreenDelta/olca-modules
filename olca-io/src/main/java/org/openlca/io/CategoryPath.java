package org.openlca.io;

import org.openlca.core.model.Category;
import org.openlca.util.Strings;

public class CategoryPath {

	private CategoryPath() {
	}

	public static String getFull(Category category) {
		return category != null
				? category.toPath()
				: "";
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
