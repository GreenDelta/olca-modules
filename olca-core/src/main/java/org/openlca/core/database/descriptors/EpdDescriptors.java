package org.openlca.core.database.descriptors;

import org.openlca.core.database.EpdDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.EpdDescriptor;

public class EpdDescriptors
		extends RootDescriptorReader<EpdDescriptor> {

	private EpdDescriptors(IDatabase db) {
		super(new EpdDao(db));
	}

	public static EpdDescriptors of(IDatabase db) {
		return new EpdDescriptors(db);
	}
}
