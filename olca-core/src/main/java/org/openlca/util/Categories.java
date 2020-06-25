package org.openlca.util;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

/**
 * Utility functions for openLCA categories.
 */
public class Categories {

	private Categories() {
	}

	public static String createRefId(Category category) {
		if (category == null)
			return null;
		List<String> path = path(category);
		ModelType type = category.modelType;
		if (type != null)
			path.add(0, type.name());
		return KeyGen.get(path.toArray(new String[0]));
	}

	public static List<String> path(Category category) {
		List<String> path = new ArrayList<>();
		Category c = category;
		while (c != null) {
			String item = c.name;
			if (item == null)
				item = "";
			path.add(0, item.trim());
			c = c.category;
		}
		return path;
	}

}
