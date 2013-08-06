package org.openlca.core.database;

import org.openlca.core.model.Location;

public class LocationDao extends BaseDao<Location> {

	public LocationDao(IDatabase database) {
		super(Location.class, database);
	}

}
