package org.openlca.core.database;

import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupDao extends
		CategorizedEntityDao<UnitGroup, UnitGroupDescriptor> {

	public UnitGroupDao(IDatabase database) {
		super(UnitGroup.class, UnitGroupDescriptor.class, database);
	}

}
