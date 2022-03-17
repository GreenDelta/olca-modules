package org.openlca.core.database;

import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class UnitDao extends RefEntityDao<Unit, UnitDescriptor> {

	public UnitDao(IDatabase database) {
		super(Unit.class, UnitDescriptor.class, database);
	}

}
