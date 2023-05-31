package org.openlca.core.database.descriptors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;

public class SocialIndicatorDescriptors
		extends RootDescriptorReader<SocialIndicatorDescriptor> {

	private SocialIndicatorDescriptors(IDatabase db) {
		super(new SocialIndicatorDao(db));
	}

	public static SocialIndicatorDescriptors of(IDatabase db) {
		return new SocialIndicatorDescriptors(db);
	}
}
