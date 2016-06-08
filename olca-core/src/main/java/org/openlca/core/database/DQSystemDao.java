package org.openlca.core.database;

import org.openlca.core.model.DQSystem;
import org.openlca.core.model.descriptors.DQSystemDescriptor;

public class DQSystemDao extends CategorizedEntityDao<DQSystem, DQSystemDescriptor> {

	public DQSystemDao(IDatabase database) {
		super(DQSystem.class, DQSystemDescriptor.class, database);
	}

}
