package org.openlca.io.ilcd.input;

import org.openlca.core.model.ModelType;

/**
 * The import for ILCD categories (classifications) into the openLCA database.
 */
public class CategoryImport extends AbstractCategoryImport<org.openlca.ilcd.commons.Category> {

	public CategoryImport(ImportConfig config, ModelType modelType) {
		super(config, modelType);
	}

	@Override
	protected String getName(org.openlca.ilcd.commons.Category ilcdClass) {
		String name = null;
		if (ilcdClass != null)
			name = ilcdClass.value;
		return name != null ? name : "";
	}

}
