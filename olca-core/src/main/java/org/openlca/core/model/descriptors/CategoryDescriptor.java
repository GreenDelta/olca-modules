package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class CategoryDescriptor extends CategorizedDescriptor {

	public ModelType categoryType;

	public CategoryDescriptor() {
		this.type = ModelType.CATEGORY;
	}

	@Override
	public CategoryDescriptor copy() {
		var copy = new CategoryDescriptor();
		copyFields(this, copy);
		copy.categoryType = categoryType;
		return copy;
	}

}
