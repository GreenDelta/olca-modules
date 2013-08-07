package org.openlca.core.database;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class BaseEntityDao<T extends RootEntity> extends
		RootEntityDao<T, BaseDescriptor> {

	public BaseEntityDao(Class<T> entityType, IDatabase database) {
		super(entityType, BaseDescriptor.class, database);
	}

}
