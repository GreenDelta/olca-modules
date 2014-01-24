package org.openlca.core.database;

import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.NwSetDescriptor;

public class NwSetDao extends RootEntityDao<NwSet, NwSetDescriptor> {

	public NwSetDao(IDatabase database) {
		super(NwSet.class, NwSetDescriptor.class, database);
	}

}
