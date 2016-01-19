package org.openlca.io.ilcd.input;

import org.openlca.core.model.ModelType;

/**
 * The import for ILCD categories (classifications) into the openLCA database.
 */
class CategoryImport extends
		AbstractCategoryImport<org.openlca.ilcd.commons.Class> {

	public CategoryImport(ImportConfig config, ModelType modelType) {
		super(config, modelType);
	}

	@Override
	protected String getName(org.openlca.ilcd.commons.Class ilcdClass) {
		String name = null;
		if (ilcdClass != null)
			name = ilcdClass.getValue();
		return name != null ? name : "";
	}

}
