package org.openlca.io.ilcd.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.ilcd.commons.Category;

/**
 * The import of ILCD compartments (elementary flow categories) into an openLCA
 * database.
 * 
 * 
 * @author Michael Srocka
 * 
 */
public class CompartmentImport extends AbstractCategoryImport<Category> {

	public CompartmentImport(IDatabase database) {
		super(database, ModelType.FLOW);
	}

	@Override
	protected String getName(Category ilcdCategory) {
		String name = null;
		if (ilcdCategory != null)
			name = ilcdCategory.getValue();
		return name == null ? "" : name;
	}
}
