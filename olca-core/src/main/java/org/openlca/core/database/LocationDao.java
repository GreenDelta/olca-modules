package org.openlca.core.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.LocationDescriptor;

public class LocationDao
		extends CategorizedEntityDao<Location, LocationDescriptor> {

	public LocationDao(IDatabase database) {
		super(Location.class, LocationDescriptor.class, database);
	}

	/**
	 * Get the location codes from the database in a map: location id ->
	 * location code.
	 */
	public Map<Long, String> getCodes() {
		if (database == null)
			return Collections.emptyMap();
		String sql = "select id, code from tbl_locations";
		Map<Long, String> map = new HashMap<>();
		try {
			NativeSql.on(database).query(sql, r -> {
				map.put(r.getLong(1), r.getString(2));
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return map;
	}

}
