package org.openlca.core.database;

import org.openlca.core.model.CostCategory;
import org.openlca.core.model.descriptors.CostCategoryDescriptor;

public class CostCategoryDao
		extends CategorizedEntityDao<CostCategory, CostCategoryDescriptor> {

	public CostCategoryDao(IDatabase db) {
		super(CostCategory.class, CostCategoryDescriptor.class, db);
	}

}
