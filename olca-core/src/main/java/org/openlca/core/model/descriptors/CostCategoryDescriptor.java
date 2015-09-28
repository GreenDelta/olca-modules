package org.openlca.core.model.descriptors;

import org.openlca.core.model.ModelType;

public class CostCategoryDescriptor extends CategorizedDescriptor {

	private static final long serialVersionUID = -5300691754684395724L;

	@Override
	public ModelType getModelType() {
		return ModelType.COST_CATEGORY;
	}
}
