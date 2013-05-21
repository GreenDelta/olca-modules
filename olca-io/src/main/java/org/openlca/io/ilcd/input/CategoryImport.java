package org.openlca.io.ilcd.input;

import org.openlca.core.database.IDatabase;

/**
 * The import for ILCD categories (classifications) into the openLCA database.
 * 
 * @author Michael Srocka
 * 
 */
class CategoryImport extends
		AbstractCategoryImport<org.openlca.ilcd.commons.Class> {

	public CategoryImport(IDatabase database, Class<?> modelType) {
		super(database, modelType);
	}

	@Override
	protected String getName(org.openlca.ilcd.commons.Class ilcdClass) {
		String name = null;
		if (ilcdClass != null)
			name = ilcdClass.getValue();
		return name != null ? name : "";
	}

}
