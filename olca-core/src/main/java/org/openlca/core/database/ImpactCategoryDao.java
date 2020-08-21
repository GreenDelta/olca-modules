package org.openlca.core.database;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactCategoryDao extends
		CategorizedEntityDao<ImpactCategory, ImpactCategoryDescriptor> {

	public ImpactCategoryDao(IDatabase database) {
		super(ImpactCategory.class, ImpactCategoryDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] {
				"id",
				"ref_id",
				"name",
				"description",
				"version",
				"last_change",
				"f_category",
				"library",
				"tags",
				"reference_unit",
		};
	}

	@Override
	protected ImpactCategoryDescriptor createDescriptor(Object[] record) {
		if (record == null)
			return null;
		var d = super.createDescriptor(record);
		d.referenceUnit = (String) record[9];
		return d;
	}
}
