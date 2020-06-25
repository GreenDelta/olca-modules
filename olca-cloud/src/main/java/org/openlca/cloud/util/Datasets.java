package org.openlca.cloud.util;

import java.util.ArrayList;
import java.util.List;

import org.openlca.cloud.model.data.Dataset;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.Descriptor;

public class Datasets {

	public static Dataset toDataset(CategorizedEntity entity) {
		CategorizedDescriptor descriptor = Descriptor.of(entity);
		Category category = entity.category;
		return toDataset(descriptor, category);
	}

	public static Dataset toDataset(CategorizedDescriptor descriptor, Category category) {
		Dataset dataset = new Dataset();
		dataset.name = descriptor.name;
		dataset.refId = descriptor.refId;
		dataset.type = descriptor.type;
		dataset.version = Version.asString(descriptor.version);
		dataset.lastChange = descriptor.lastChange;
		ModelType categoryType = null;
		if (category != null) {
			dataset.categoryRefId = category.refId;
			categoryType = category.modelType;
		} else {
			if (descriptor.type == ModelType.CATEGORY)
				categoryType = ((CategoryDescriptor) descriptor).categoryType;
			else
				categoryType = descriptor.type;
		}
		dataset.categoryType = categoryType;
		dataset.categories = getCategories(descriptor, category);
		return dataset;
	}

	public static List<String> getCategories(Category category) {
		return getCategories(Descriptor.of(category), category.category);
	}

	public static List<String> getCategories(CategorizedDescriptor entity, Category category) {
		List<String> categories = new ArrayList<>();
		while (category != null) {
			categories.add(0, category.name);
			category = category.category;
		}
		return categories;
	}
	
}
