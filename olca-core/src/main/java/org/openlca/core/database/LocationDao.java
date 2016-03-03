package org.openlca.core.database;

import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class LocationDao extends CategorizedEntityDao<Location, LocationDescriptor> {

	public LocationDao(IDatabase database) {
		super(Location.class, LocationDescriptor.class, database);
	}

}
