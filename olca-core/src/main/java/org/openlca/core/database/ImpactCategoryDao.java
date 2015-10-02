package org.openlca.core.database;

import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class ImpactCategoryDao extends
		RootEntityDao<ImpactCategory, ImpactCategoryDescriptor> {

	public ImpactCategoryDao(IDatabase database) {
		super(ImpactCategory.class, ImpactCategoryDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "version", "last_change",
				"reference_unit" };
	}

	@Override
	protected ImpactCategoryDescriptor createDescriptor(Object[] queryResult) {
		if (queryResult == null)
			return null;
		ImpactCategoryDescriptor d = super.createDescriptor(queryResult);
		d.setReferenceUnit((String) queryResult[6]);
		return d;
	}

}
