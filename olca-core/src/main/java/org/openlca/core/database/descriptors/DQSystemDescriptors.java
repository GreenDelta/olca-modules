package org.openlca.core.database.descriptors;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.DQSystemDescriptor;

public class DQSystemDescriptors
		extends RootDescriptorReader<DQSystemDescriptor> {

	private DQSystemDescriptors(IDatabase db) {
		super(new DQSystemDao(db));
	}

	public static DQSystemDescriptors of(IDatabase db) {
		return new DQSystemDescriptors(db);
	}
}
