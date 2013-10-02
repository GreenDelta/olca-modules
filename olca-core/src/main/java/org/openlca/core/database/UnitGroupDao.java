package org.openlca.core.database;

import java.util.Collections;

import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupDao extends
		CategorizedEntityDao<UnitGroup, UnitGroupDescriptor> {

	public UnitGroupDao(IDatabase database) {
		super(UnitGroup.class, UnitGroupDescriptor.class, database);
	}

	public UnitGroup getForUnit(String name) {
		if (name == null)
			return null;
		String jpql = "select ug from UnitGroup ug join ug.units u where u.name = :name";
		try {
			return Query.on(getDatabase()).getFirst(entityType, jpql,
					Collections.singletonMap("name", name));
		} catch (Exception e) {
			log.error("failed to get instance for name " + name, e);
			return null;
		}
	}

}
