package org.openlca.core.database;

import org.openlca.core.model.Direction;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.descriptors.ImpactDescriptor;

public class ImpactCategoryDao extends
        RootEntityDao<ImpactCategory, ImpactDescriptor> {

	public ImpactCategoryDao(IDatabase database) {
		super(ImpactCategory.class, ImpactDescriptor.class, database);
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
				"direction",
		};
	}

	@Override
	protected ImpactDescriptor createDescriptor(Object[] record) {
		if (record == null)
			return null;
		var d = super.createDescriptor(record);
		d.referenceUnit = (String) record[9];
		if (record[10] instanceof String s) {
			d.direction = Direction.from(s);
		}
		return d;
	}
}
