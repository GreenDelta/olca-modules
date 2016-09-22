package org.openlca.io.ilcd.input;

import org.openlca.core.model.ModelType;
import org.openlca.ilcd.commons.Category;

/**
 * The import of ILCD compartments (elementary flow categories) into an openLCA
 * database.
 */
public class CompartmentImport extends AbstractCategoryImport<Category> {

	public CompartmentImport(ImportConfig config) {
		super(config, ModelType.FLOW);
	}

	@Override
	protected String getName(Category ilcdCategory) {
		String name = null;
		if (ilcdCategory != null)
			name = ilcdCategory.value;
		return name == null ? "" : name;
	}
}
