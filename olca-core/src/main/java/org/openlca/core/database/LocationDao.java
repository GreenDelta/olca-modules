package org.openlca.core.database;

import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class LocationDao extends RootEntityDao<Location, BaseDescriptor> {

	public LocationDao(IDatabase database) {
		super(Location.class, BaseDescriptor.class, database);
	}

}
