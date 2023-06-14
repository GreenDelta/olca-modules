package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.descriptors.SourceDescriptor;

public class SourceDescriptors
		extends RootDescriptorReader<SourceDescriptor> {

	private SourceDescriptors(IDatabase db) {
		super(new SourceDao(db));
	}

	public static SourceDescriptors of(IDatabase db) {
		return new SourceDescriptors(db);
	}
}
