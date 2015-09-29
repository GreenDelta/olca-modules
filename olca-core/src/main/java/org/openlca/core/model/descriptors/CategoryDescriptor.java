package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class CategoryDescriptor extends CategorizedDescriptor {

	private static final long serialVersionUID = -6749748089841757123L;

	public CategoryDescriptor() {
		setType(ModelType.CATEGORY);
	}

	private ModelType categoryType;

	public ModelType getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(ModelType categoryType) {
		this.categoryType = categoryType;
	}

}