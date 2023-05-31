package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

public class ImpactMethodDescriptors
		extends RootDescriptorReader<ImpactMethodDescriptor> {

	private ImpactMethodDescriptors(IDatabase db) {
		super(new ImpactMethodDao(db));
	}

	public static ImpactMethodDescriptors of(IDatabase db) {
		return new ImpactMethodDescriptors(db);
	}
}
