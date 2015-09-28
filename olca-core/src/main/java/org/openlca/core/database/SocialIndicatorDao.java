package org.openlca.core.database;

import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;

public class SocialIndicatorDao
		extends CategorizedEntityDao<SocialIndicator, SocialIndicatorDescriptor> {

	public SocialIndicatorDao(IDatabase database) {
		super(SocialIndicator.class, SocialIndicatorDescriptor.class, database);
	}

}
