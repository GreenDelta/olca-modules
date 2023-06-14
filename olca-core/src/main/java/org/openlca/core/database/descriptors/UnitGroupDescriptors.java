package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupDescriptors
		extends RootDescriptorReader<UnitGroupDescriptor> {

	private UnitGroupDescriptors(IDatabase db) {
		super(new UnitGroupDao(db));
	}

	public static UnitGroupDescriptors of(IDatabase db) {
		return new UnitGroupDescriptors(db);
	}
}
