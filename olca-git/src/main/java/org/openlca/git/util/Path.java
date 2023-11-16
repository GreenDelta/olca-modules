package org.openlca.git.util;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.Categories;
import org.openlca.util.Strings;
import org.openlca.util.Categories.PathBuilder;

public class Path {

	public static String of(ModelType t) {
		return t.name();
	}

	public static String of(Category c) {
		var paths = Categories.path(c);
		paths.add(0, c.modelType.name());
		return Strings.join(paths, '/');
	}

	public static String of(PathBuilder categoryPaths, RootDescriptor d) {
		if (d.type == ModelType.CATEGORY)
			return d.type.name() + "/" + categoryPaths.pathOf(d.id);
		var categoryPath = categoryPaths.pathOf(d.category);
		if (Strings.nullOrEmpty(categoryPath))
			return d.type.name() + "/" + d.refId + GitUtil.DATASET_SUFFIX;
		return d.type.name() + "/" + categoryPath + "/" + d.refId + GitUtil.DATASET_SUFFIX;
	}

}
